# Renewable sign-in sessions

The backend now returns a short-lived access token and an opaque refresh token.
The frontend must implement renewal to keep users signed in. Existing logins have
no refresh token, so users need to sign in once after this change.

## Login

`POST /api/auth/login` accepts the existing email/password body. The response is:

```json
{
  "token": "access JWT",
  "expiresIn": 900000,
  "refreshToken": "opaque session credential",
  "refreshExpiresIn": 2592000000
}
```

Durations are milliseconds: access tokens last 15 minutes; refresh sessions last
30 days since login or the last successful refresh. Each refresh resets that
30-day window. Both settings are configurable in application.properties.

## Frontend renewal

1. Send the access token as `Authorization: Bearer <token>` on API requests.
2. On app startup, renew the session if the access token has expired or is missing.
   During use, renew shortly before expiry or upon an API HTTP 401 response.
3. Call `POST /api/auth/refresh` with JSON `{"refreshToken":"saved credential"}`.
   No access token is required. An expired Authorization header is ignored here.
4. Save **both new tokens** from the response, then retry the original request
   once. The previous refresh token is invalid after rotation.
5. Serialize refresh requests, including across tabs. Concurrent requests should
   await the same refresh result, rather than each submitting the old credential.
6. A refresh HTTP 401 means sign-in is required. A network or server failure should
   allow a retry, rather than immediately deleting the session.
7. Refresh transactions/dashboard data normally after authentication is restored.

Treat refresh tokens as credentials: use HTTPS, exclude them from logs and URLs,
and store them in platform-protected storage where available. For a browser app,
a backend-for-frontend with an HttpOnly cookie can keep the refresh credential
outside JavaScript; this API currently exchanges it in JSON and sets no cookies.
No credentials are automatically attached by the browser to these auth endpoints.

## Logout

Call `POST /api/auth/logout` with `{"refreshToken":"current credential"}`, then
clear local credentials. The endpoint returns 204 and revokes this device's
refresh session. Already-issued access tokens remain valid until expiry (up to
15 minutes for new tokens). Other devices keep their separate sessions.

## Deployment

Flyway migration V10 creates `refresh_sessions`; only SHA-256 hashes of refresh
tokens are stored. Refresh and logout lock the session row to prevent concurrent
reuse. Deleting a user cascades to their sessions. Previously issued access tokens
retain their original expiry. Deploy the frontend renewal flow alongside this
backend change, since access tokens now have a shorter lifetime.
