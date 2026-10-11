# Reimbursements with optional expense links

An incoming payment can be marked as a reimbursement without choosing an
original expense. This supports a single payment covering many expenses.
Existing linked reimbursements remain supported.

## Mark an existing income transaction

`PATCH /api/transactions/{id}`:

```json
{
  "transactionNature": "REIMBURSEMENT"
}
```

To also remove an existing expense link, send:

```json
{
  "transactionNature": "REIMBURSEMENT",
  "reimbursementForTransactionId": null
}
```

Omitting `reimbursementForTransactionId` on PATCH preserves any existing link;
explicit `null` clears it. A non-null ID links to an expense owned by the user
and still enforces the remaining reimbursable amount. Changing the nature to
`NORMAL` clears the link and treats the payment as normal income again.
Marking a transaction does not change its amount or account balance.

## Create a standalone reimbursement

`POST /api/transactions` (replace the funding source ID with your account):

```json
{
  "type": "INCOME",
  "transactionNature": "REIMBURSEMENT",
  "amount": 250.00,
  "destinationFundingSourceId": 4,
  "transactionDate": "2026-10-02",
  "description": "Reimbursement for several expenses"
}
```

The expense ID may be omitted or null. The response has
`transactionNature: "REIMBURSEMENT"` and null expense-link fields. Creating the
payment credits the destination account as usual.

## Dashboard and frontend

- All reimbursements reduce all-time net expenses and are excluded from income.
- Linked reimbursements reduce spending in the original expense's month/category.
- Unlinked reimbursements reduce spending in their own transaction month, even
  if that month has no expenses (net spending can be negative).
- Unlinked reimbursements are not allocated to expense categories, so category
  totals will not include their deductions. They do not reduce any individual
  expense's remaining reimbursable amount.
- The frontend should offer “Mark as reimbursement” on income transactions and
  make the original-expense selector optional. Send explicit null when removing
  a selected link, then refresh transaction and dashboard data.

No database migration is required; the expense link is already nullable.
