package com.money.finance_tracker.service;

import com.money.finance_tracker.dto.*;
import com.money.finance_tracker.entity.*;
import com.money.finance_tracker.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
import static com.money.finance_tracker.entity.TransactionStatus.*;
import static com.money.finance_tracker.service.ForecastService.Horizon.*;

@EnabledIfEnvironmentVariable(named = "FINANCE_TEST_DB", matches = ".+")
@SpringBootTest(properties = {"spring.datasource.url=${FINANCE_TEST_DB}", "finance.scheduling.enabled=false",
        "logging.level.root=WARN", "logging.level.org.flywaydb=WARN", "spring.jpa.show-sql=false"})
class SchedulingIntegrationTest {
    @TestConfiguration
    static class ClockConfiguration {
        @Bean @Primary MutableClock testClock() { return new MutableClock(); }
    }
    static class MutableClock extends AccountClock {
        volatile Instant instant = Instant.parse("2026-10-11T12:00:00Z");
        @Override public LocalDate today(User user) { return instant.atZone(ZoneId.of(user.getTimezone())).toLocalDate(); }
    }
    @Autowired org.springframework.web.context.WebApplicationContext webContext;
    @Autowired MutableClock clock;
    @Autowired TransactionService service;
    @Autowired ForecastService forecasts;
    @Autowired DashboardService dashboard;
    @Autowired SubscriptionService subscriptionService;
    @Autowired AccountLedgerService ledger;
    @Autowired UserRepository users;
    @Autowired FundingSourceRepository sources;
    @Autowired CategoryRepository categories;
    @Autowired TransactionRepository transactions;
    @Autowired SubscriptionRepository subscriptions;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager manager;
    User user;
    Long sourceId, categoryId;
    <T> T tx(Supplier<T> supplier) { return new TransactionTemplate(manager).execute(status -> supplier.get()); }
    @BeforeEach void setup() {
        clock.instant = Instant.parse("2026-10-11T12:00:00Z");
        user = tx(() -> {
            User u = new User(); u.setName("Scheduling test"); u.setEmail(UUID.randomUUID()+"@test.invalid"); u.setPasswordHash("test");
            users.save(u);
            FundingSource source = new FundingSource(); source.setName("Main"); source.setUser(u);
            source.setBalance(new BigDecimal("2000")); source.setInitialBalance(new BigDecimal("2000"));
            sourceId = sources.save(source).getId();
            Category category = new Category(); category.setUser(u); category.setName("Costs");
            categoryId = categories.save(category).getId();
            return u;
        });
    }
    @AfterEach void cleanup() {
        if (user == null) return;
        tx(() -> {
            jdbc.update("delete from subscription_payments where subscription_id in (select id from subscriptions where user_id=?)", user.getId());
            jdbc.update("delete from transactions where user_id=?", user.getId());
            jdbc.update("delete from subscriptions where user_id=?", user.getId());
            jdbc.update("delete from users where id=?", user.getId());
            return null;
        });
    }
    TransactionDto expense(String date, String amount) {
        TransactionDto dto = new TransactionDto(); dto.setType(TransactionTypeEnum.EXPENSE);
        dto.setTransactionDate(LocalDate.parse(date)); dto.setAmount(new BigDecimal(amount));
        dto.setSourceFundingSourceId(sourceId); dto.setCategoryId(categoryId); dto.setDescription("Rent"); return dto;
    }
    BigDecimal balance() { return sources.findById(sourceId).orElseThrow().getBalance(); }
    void money(String expected, BigDecimal actual) { assertEquals(0, new BigDecimal(expected).compareTo(actual), actual.toString()); }
    ForecastResponseDto.FundingSourceForecast forecast() {
        return forecasts.forecast(user, NEXT_MONTH).fundingSources().stream().filter(s -> s.id().equals(sourceId)).findFirst().orElseThrow();
    }
    SubscriptionResponseDto subscription(String date, SubscriptionFrequency frequency, String amount) {
        SubscriptionDto dto = new SubscriptionDto(); dto.setName("Plan"); dto.setAmount(new BigDecimal(amount));
        dto.setFundingSourceId(sourceId); dto.setCategoryId(categoryId); dto.setFrequency(frequency);
        dto.setNextPaymentDate(LocalDate.parse(date)); return subscriptionService.createSubscription(dto, user);
    }
    Long generated(Long subscriptionId, String date, String amount) {
        return tx(() -> {
            Transaction t = new Transaction(); t.setUser(users.findById(user.getId()).orElseThrow());
            t.setType(TransactionTypeEnum.EXPENSE); t.setStatus(SCHEDULED); t.setAmount(new BigDecimal(amount));
            t.setSourceFundingSource(sources.findById(sourceId).orElseThrow()); t.setCategory(categories.findById(categoryId).orElseThrow());
            t.setTransactionDate(LocalDate.parse(date)); t.setOccurrenceDate(LocalDate.parse(date));
            t.setSubscription(subscriptions.findById(subscriptionId).orElseThrow());
            return transactions.save(t).getId();
        });
    }
    @Test void scheduleEditCancelAndPostedTransitions() {
        var created = service.schedule(expense("2026-11-01", "650"), user);
        assertEquals(SCHEDULED, created.getStatus()); money("2000", balance()); money("1350", forecast().predictedBalance());
        var overview = dashboard.getOverview(user, 2026);
        money("0", overview.getAllTimeExpense()); assertTrue(overview.getMonthlySpending().isEmpty());
        assertEquals(0, service.getReimbursableExpenses(user, null, PageRequest.of(0, 10)).getTotalElements());
        TransactionUpdateDto patch = new TransactionUpdateDto(); patch.setAmount(new BigDecimal("700"));
        service.patchSchedule(created.getId(), patch, user); money("1300", forecast().predictedBalance()); money("2000", balance());
        patch.setTransactionDate(LocalDate.parse("2026-10-11"));
        assertEquals(POSTED, service.patchSchedule(created.getId(), patch, user).getStatus()); money("1300", balance());
        patch.setTransactionDate(LocalDate.parse("2026-11-01"));
        assertEquals(SCHEDULED, service.patchTransaction(created.getId(), patch, user).getStatus()); money("2000", balance());
        service.deleteTransaction(created.getId(), user); money("2000", forecast().predictedBalance());
        clock.instant = Instant.parse("2026-12-01T12:00:00Z"); ledger.reconcile(user.getId()); money("2000", balance());
    }
    @Test void retriesConcurrentWorkersAndForecastStayEconomicallyConsistent() throws Exception {
        service.schedule(expense("2026-11-01", "2500"), user);
        money("-500", forecast().predictedBalance());
        clock.instant = Instant.parse("2026-11-01T12:00:00Z");
        race(() -> ledger.reconcile(user.getId()), () -> ledger.reconcile(user.getId()));
        ledger.reconcile(user.getId()); money("-500", balance()); money("-500", forecast().predictedBalance());
        money("0", forecast().scheduledExpenses()); money("2500", dashboard.getOverview(user, 2026).getAllTimeExpense());
    }
    @Test void generatedOccurrencesAreDeduplicatedHiddenBeforePaginationAndCanceledOnDeactivation() {
        var sub = subscription("2026-10-20", SubscriptionFrequency.MONTHLY, "20");
        Long generated = generated(sub.getId(), "2026-10-20", "25");
        service.schedule(expense("2026-11-01", "650"), user);
        money("45", forecast().subscriptionExpenses()); money("650", forecast().scheduledExpenses()); money("1305", forecast().predictedBalance());
        var page = service.getTransactions(user, null, null, null, null, null, null, null, PageRequest.of(0, 1));
        assertEquals(1, page.getTotalElements()); assertEquals(1, page.getTotalPages());
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.deleteTransaction(generated, user)).getStatusCode().value());
        subscriptionService.deactivateSubscription(sub.getId(), user);
        money("0", forecast().subscriptionExpenses()); assertFalse(transactions.existsById(generated));
        clock.instant = Instant.parse("2026-10-25T12:00:00Z"); ledger.reconcile(user.getId()); money("2000", balance());
    }
    @Test void catchupPreservesMonthAnchorAndDeletedPaymentDoesNotReplay() {
        clock.instant = Instant.parse("2026-03-01T12:00:00Z");
        var sub = subscription("2026-01-31", SubscriptionFrequency.MONTHLY, "20");
        assertEquals(LocalDate.parse("2026-03-31"), sub.getNextPaymentDate()); money("1960", balance());
        money("40", forecast().subscriptionExpenses());
        var payment = service.getTransactions(user, null, null, null, null, null, null, null, PageRequest.of(0, 10)).getContent().getFirst();
        assertEquals(POSTED, payment.getStatus()); service.deleteTransaction(payment.getId(), user); money("1980", balance());
        SubscriptionUpdateDto patch = new SubscriptionUpdateDto(); patch.setNextPaymentDate(LocalDate.parse("2026-01-31"));
        subscriptionService.updateSubscription(sub.getId(), patch, user); money("1980", balance());
        ledger.reconcile(user.getId()); money("1980", balance());
    }
    @Test void generatedPaymentPostsOnceAndMaintainsHistory() {
        var sub = subscription("2026-10-20", SubscriptionFrequency.MONTHLY, "20");
        Long id = generated(sub.getId(), "2026-10-20", "25");
        clock.instant = Instant.parse("2026-10-20T12:00:00Z"); ledger.reconcile(user.getId()); ledger.reconcile(user.getId());
        money("1975", balance()); assertEquals(POSTED, service.getTransactionById(id, user).getStatus());
        money("20", forecast().subscriptionExpenses());
        subscriptionService.deactivateSubscription(sub.getId(), user);
        assertTrue(transactions.existsById(id)); money("0", forecast().subscriptionExpenses());
    }
    @Test void reimbursementsBlockMovingPostedExpenseAndRejectUnpostedReferences() {
        var expense = service.addTransaction(expense("2026-10-10", "100"), user);
        TransactionDto refund = new TransactionDto(); refund.setType(TransactionTypeEnum.INCOME); refund.setAmount(new BigDecimal("25"));
        refund.setDestinationFundingSourceId(sourceId); refund.setTransactionDate(LocalDate.parse("2026-10-11"));
        refund.setTransactionNature(TransactionNature.REIMBURSEMENT); refund.setReimbursementForTransactionId(expense.getId());
        service.addTransaction(refund, user);
        TransactionUpdateDto patch = new TransactionUpdateDto(); patch.setTransactionDate(LocalDate.parse("2026-11-01"));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> service.patchSchedule(expense.getId(), patch, user)).getStatusCode().value());
        money("1925", balance()); assertEquals(POSTED, service.getTransactionById(expense.getId(), user).getStatus());
        var scheduled = service.schedule(expense("2026-11-01", "100"), user);
        refund.setReimbursementForTransactionId(scheduled.getId());
        assertThrows(IllegalArgumentException.class, () -> service.addTransaction(refund, user)); money("1925", balance());
    }
    @Test void duePostingRacesWithCancellationAndRescheduling() throws Exception {
        var expense = service.schedule(expense("2026-11-01", "100"), user);
        clock.instant = Instant.parse("2026-11-01T12:00:00Z");
        race(() -> ledger.reconcile(user.getId()), () -> service.deleteTransaction(expense.getId(), user));
        money("2000", balance()); assertFalse(transactions.existsById(expense.getId()));
        clock.instant = Instant.parse("2026-10-11T12:00:00Z");
        var other = service.schedule(expense("2026-11-01", "100"), user);
        clock.instant = Instant.parse("2026-11-01T12:00:00Z");
        TransactionUpdateDto patch = new TransactionUpdateDto(); patch.setTransactionDate(LocalDate.parse("2026-12-01"));
        race(() -> ledger.reconcile(user.getId()), () -> service.patchSchedule(other.getId(), patch, user));
        money("2000", balance()); assertEquals(SCHEDULED, service.getTransactionById(other.getId(), user).getStatus());
        money("1900", forecast().predictedBalance());
    }
    @Test void validatesDatesAndOwnershipAndUsesAccountTimezone() {
        assertThrows(IllegalArgumentException.class, () -> service.schedule(expense("2026-10-11", "10"), user));
        TransactionDto dto = expense("2026-11-01", "10"); dto.setType(TransactionTypeEnum.INCOME);
        assertThrows(IllegalArgumentException.class, () -> service.schedule(dto, user));
        assertThrows(IllegalArgumentException.class, () -> service.addTransaction(dto, user));
        var expense = service.schedule(expense("2026-11-01", "10"), user);
        User other = tx(() -> { User u = new User(); u.setName("Other"); u.setEmail(UUID.randomUUID()+"@test.invalid"); u.setPasswordHash("test"); return users.save(u); });
        try {
            assertThrows(EntityNotFoundException.class, () -> service.getTransactionById(expense.getId(), other));
            assertThrows(EntityNotFoundException.class, () -> service.schedule(expense("2026-11-01", "10"), other));
            assertThrows(EntityNotFoundException.class, () -> service.patchSchedule(expense.getId(), new TransactionUpdateDto(), other));
            assertThrows(EntityNotFoundException.class, () -> service.deleteTransaction(expense.getId(), other));
        } finally { users.deleteById(other.getId()); }
        tx(() -> { users.findById(user.getId()).orElseThrow().setTimezone("America/Los_Angeles"); return null; });
        clock.instant = Instant.parse("2026-11-01T01:00:00Z");
        assertEquals(LocalDate.parse("2026-10-31"), forecasts.forecast(user, THIS_MONTH).asOf());
        money("2000", balance());
        clock.instant = Instant.parse("2026-11-01T09:00:00Z"); ledger.reconcile(user.getId()); money("1990", balance());
    }
    @Test void yearlyLeapOccurrencesHorizonRolloverAndEmptySources() {
        clock.instant = Instant.parse("2027-12-11T12:00:00Z");
        var forecast = forecasts.forecast(user, NEXT_MONTH); assertEquals(LocalDate.parse("2028-01-31"), forecast.through());
        assertEquals(LocalDate.parse("2027-12-31"), forecasts.forecast(user, THIS_MONTH).through());
        subscription("2028-02-29", SubscriptionFrequency.YEARLY, "100"); money("0", forecast().subscriptionExpenses());
        clock.instant = Instant.parse("2028-01-31T12:00:00Z");
        assertEquals(LocalDate.parse("2028-02-29"), forecasts.forecast(user, NEXT_MONTH).through()); money("100", forecast().subscriptionExpenses());
        clock.instant = Instant.parse("2029-02-28T12:00:00Z"); ledger.reconcile(user.getId()); money("1800", balance());
        var subscription = subscriptionService.getSubscriptions(user).getFirst();
        assertEquals(LocalDate.parse("2030-02-28"), subscription.getNextPaymentDate());
        assertEquals(1, forecasts.forecast(user, NEXT_MONTH).fundingSources().size()); money("0", forecast().subscriptionExpenses());
    }
    @Test void changingSourceUpdatesOnlyThatSourcesPrediction() {
        Long second = tx(() -> {
            FundingSource source = new FundingSource(); source.setName("Other"); source.setUser(user);
            source.setBalance(new BigDecimal("500")); source.setInitialBalance(new BigDecimal("500"));
            return sources.save(source).getId();
        });
        var scheduled = service.schedule(expense("2026-11-01", "100"), user);
        assertEquals(2, forecasts.forecast(user, NEXT_MONTH).fundingSources().size());
        TransactionUpdateDto patch = new TransactionUpdateDto(); patch.setSourceFundingSourceId(second);
        service.patchSchedule(scheduled.getId(), patch, user);
        money("2000", balance()); money("2000", forecast().predictedBalance());
        var other = forecasts.forecast(user, NEXT_MONTH).fundingSources().stream().filter(f -> f.id().equals(second)).findFirst().orElseThrow();
        money("400", other.predictedBalance()); money("500", sources.findById(second).orElseThrow().getBalance());
    }

    @Test void httpContractValidatesCalendarDatesAndReturnsStatusesAndHorizonErrors() throws Exception {
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(webContext).build();
        var security = org.springframework.security.core.context.SecurityContextHolder.getContext();
        security.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(user, null, List.of()));
        String template = "{\"type\":\"EXPENSE\",\"amount\":10,\"transactionDate\":\"%s\",\"sourceFundingSourceId\":%d,\"categoryId\":%d}";
        try {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/transactions/scheduled")
                    .contentType("application/json").content(template.formatted("2027-02-30", sourceId, categoryId)))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/transactions/scheduled")
                    .contentType("application/json").content(template.formatted("2026-10-11", sourceId, categoryId)))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
            var result = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/transactions/scheduled")
                    .contentType("application/json").content(template.formatted("2026-11-01", sourceId, categoryId)))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated()).andReturn();
            assertTrue(result.getResponse().getContentAsString().contains("\"status\":\"SCHEDULED\""));
            var forecast = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/dashboard/forecast"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andReturn();
            assertTrue(forecast.getResponse().getContentAsString().contains("2026-11-30"));
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/dashboard/forecast?horizon=INVALID"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
        } finally { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
    }

    void race(Runnable a, Runnable b) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> { await(start); a.run(); });
            var second = executor.submit(() -> { await(start); b.run(); });
            start.countDown(); first.get(15, TimeUnit.SECONDS); second.get(15, TimeUnit.SECONDS);
        }
    }
    void await(CountDownLatch latch) { try { latch.await(); } catch (InterruptedException e) { throw new RuntimeException(e); } }
}
