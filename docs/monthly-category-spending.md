# Monthly category spending

`GET /api/dashboard/overview?year=2026` includes `monthlySpendingByCategory`,
a complete, unpaginated array of `{month, categoryId, categoryName, amount}`.
The month is 1–12 and amount is a JSON number. Empty years return `[]`.
Existing response fields retain their behavior. Invalid years outside 1–9999
return HTTP 400. The upper exclusive bound for 9999 is January 1, 10000,
which Java LocalDate and PostgreSQL DATE support.

Both category arrays use posted expenses belonging to the authenticated user
and the transaction's stored calendar date, including posted subscription
payments. Reconciliation uses the account timezone before reading totals.
Scheduled expenses, unposted occurrences, transfers, and normal income are
excluded. Category joins preserve the annual behavior; the current schema
requires categories on expenses and prevents deletion of referenced categories.

Linked posted reimbursements reduce the original expense's month and category,
even when the receipt is in another year. Standalone reimbursements do not
reduce category totals. Existing transaction validation caps linked refunds at
the expense amount. Aggregation uses BigDecimal over stored NUMERIC(14,2)
amounts without additional rounding or clamping. Monthly category amounts sum
to the corresponding annual category amount.

No month parameter or additional endpoint is needed. The frontend filters the
array locally and resets to All year when the dashboard year changes. An older
backend without this field can still display the annual chart; a month selection
should display “Monthly category spending is not available yet.”

Unit coverage lives in DashboardServiceTest. Database coverage in
SchedulingIntegrationTest requires FINANCE_TEST_DB pointing to a disposable
PostgreSQL database (see scheduled-expenses.md). UI behavior must be verified
in the frontend application.
