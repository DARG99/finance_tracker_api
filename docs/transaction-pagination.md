# Transactions per page

The transactions API already supports selectable page sizes. The frontend can
offer a “Transactions per page” dropdown with **20**, **50**, and **100** options.
The API defaults to **50** when `size` is omitted.

```http
GET /api/transactions?page=0&size=20
GET /api/transactions?page=0&size=50
GET /api/transactions?page=0&size=100
```

Pages are zero-based: `page=0` is the first page, and `page=1` is the second.
The response contains transactions in `content`, with pagination fields including
`number`, `size`, `totalElements`, and `totalPages`. The last page can contain
fewer transactions than the requested size.

When the user changes the dropdown:

1. Store the selected page size.
2. Reset the current page to `0`.
3. Fetch transactions with the new `size`, retaining active filters and sorting.
4. Render the returned `content` and update pagination from the response.

Keep sending the selected `size` when navigating between pages. Reset to page
`0` when filters change as well, so a previously selected page does not fall
outside the filtered results.

No backend or database changes are required. The visible dropdown belongs in
the frontend application, which is not included in this repository.

## Category links

`GET /api/transactions?categoryId=1&from=2026-02-01&to=2026-02-28&page=0&size=50`
combines category ID and inclusive ISO calendar dates with AND, scoped to the
signed-in user. Nonpositive category IDs, malformed dates, and reversed ranges
return HTTP 400. Dates use the stored transaction calendar date, as in the overview;
December 31, 9999 needs no next-day conversion. Filtering occurs before counting
and pagination. Requested sorting is preserved with a descending ID tie-breaker
when ID is not already present.

The listing retains scheduled manual expenses and excludes unposted subscription
occurrences. These links do not add an expense or reimbursement filter, so listing
amounts need not equal dashboard net spending. Category filtering uses the stored
ID without requiring membership in the current category options.
