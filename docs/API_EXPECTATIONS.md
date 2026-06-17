# LustigeWitze Android Mobile API Contract

Source of truth: existing LustigeWitze backend in `../lustigewitze`, namespace `/api/mobile/*`.

Android must not use browser cookie auth. Native clients use JSON endpoints, camelCase response fields, ISO-8601 date strings and bearer access tokens.

## Base Rules

- Base URL: environment/build-config specific, path prefix `/api/mobile`.
- Content type for request bodies: `application/json`.
- Public reads may omit auth. If a bearer token is present and valid, viewer fields are filled.
- Protected writes require `Authorization: Bearer <accessToken>`.
- Error shape is always:

```json
{
  "error": "validation_error",
  "message": "Human-readable message.",
  "fields": {
    "content": "max 280 chars"
  }
}
```

## Auth

Access tokens are JWT bearer tokens with 15 minute TTL. Refresh tokens are JWTs with 30 day TTL, stored server-side as SHA-256 hashes and rotated on every refresh.

Android behavior:

- Store access/refresh tokens in encrypted storage.
- On `401`, attempt exactly one refresh.
- If refresh succeeds, retry the original request once.
- If refresh fails, clear local session and show signed-out UI.
- Send refresh token only to refresh/logout endpoints.

### `POST /auth/register`

Request:

```json
{
  "username": "bro_lacher",
  "email": "bro@example.com",
  "password": "secret123"
}
```

Success `201`:

```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "user": {
    "id": "uuid",
    "username": "bro_lacher",
    "email": "bro@example.com"
  }
}
```

Errors: `422 validation_error`, `409 duplicate_user`, `500 registration_failed`.

### `POST /auth/login`

Request:

```json
{
  "identifier": "bro_lacher",
  "password": "secret123"
}
```

`identifier` accepts username or email. Existing iOS-compatible payloads using `email` or `username` are also accepted.

Success `200`: same shape as register.

Errors: `401 invalid_credentials`.

### `POST /auth/refresh`

Request:

```json
{
  "refreshToken": "jwt"
}
```

Compatibility: backend also accepts `refresh_token`.

Success `200`:

```json
{
  "accessToken": "new-jwt",
  "refreshToken": "new-jwt"
}
```

Errors: `422 validation_error`, `401 invalid_refresh_token`.

### `GET /auth/me`

Header: bearer access token required.

Success `200`:

```json
{
  "user": {
    "id": "uuid",
    "username": "bro_lacher",
    "email": "bro@example.com",
    "bio": null,
    "proTier": false,
    "proSince": null,
    "createdAt": "2026-04-28T00:00:00.000Z"
  }
}
```

Errors: `401 missing_access_token`, `401 invalid_access_token`, `404 user_not_found`.

### `POST /auth/logout`

Request:

```json
{
  "refreshToken": "jwt"
}
```

Compatibility: backend also accepts `refresh_token`.

Success `200`:

```json
{ "ok": true }
```

Errors: `422 validation_error`, `401 invalid_refresh_token`.

## Shared Response Types

### `JokeItem`

```json
{
  "id": "uuid",
  "content": "Warum ...?",
  "category": "wortwitze",
  "status": "active",
  "createdAt": "2026-04-28T00:00:00.000Z",
  "author": {
    "id": "uuid",
    "username": "pointenpaule"
  },
  "score": 4,
  "favoriteCount": 2,
  "legendCount": 1,
  "viewerVote": null,
  "viewerFavorite": false
}
```

`viewerVote` is `-1`, `1`, `5` or `null`. `5` is the backend's superlike/legend value.

### `CommentItem`

```json
{
  "id": "uuid",
  "content": "Bro der war stark",
  "createdAt": "2026-04-28T00:00:00.000Z",
  "deletedAt": null,
  "parentId": null,
  "isOwner": false,
  "author": {
    "id": "uuid",
    "username": "ehrenotto"
  },
  "replies": []
}
```

## Discovery

### `GET /feed`

Query:

- `sort`: `latest` or `top`, default `latest`
- `category`: `all` or backend category value, default `all`
- `cursor`: integer offset, default `0`
- `limit`: `1..50`, default `20`
- `period`: `today`, `week`, `month` or `all`, default `all`

Success `200`:

```json
{
  "items": [],
  "nextCursor": null
}
```

Errors: `422 validation_error`.

### `GET /jokes/random`

Success `200`:

```json
{
  "joke": null
}
```

When a joke exists, `joke` is a `JokeItem`.

### `GET /jokes/:id`

Success `200`: one `JokeItem`.

Errors: `404 not_found`.

### `GET /leaderboard`

Query:

- `scope`: `jokes`, `users` or `favorited`; default `jokes`
- Compatibility: `type` may be used instead of `scope`
- `period`: `today`, `week`, `month` or `all`, default `all`
- `limit`: `1..50`, default `20`

Success `200` for joke/favorited scope:

```json
{
  "scope": "jokes",
  "period": "all",
  "items": []
}
```

Success `200` for user scope:

```json
{
  "scope": "users",
  "period": "all",
  "items": [
    {
      "id": "uuid",
      "username": "pointenpaule",
      "totalScore": 42,
      "user": {
        "id": "uuid",
        "username": "pointenpaule"
      },
      "score": 42,
      "jokeCount": 7
    }
  ]
}
```

Errors: `422 validation_error`.

## Comments

### `GET /jokes/:id/comments`

Auth optional.

Success `200`:

```json
{
  "items": []
}
```

Errors: `404 not_found`.

### `POST /jokes/:id/comments`

Header: bearer access token required.

Request:

```json
{
  "content": "Bro der war stark",
  "parentId": null
}
```

Rules:

- `content`: required, trimmed, `1..500` chars
- `parentId`: optional UUID or `null`; must belong to the same joke

Success `201`: created `CommentItem` with `isOwner: true`.

Errors: `401 unauthorized`, `404 not_found`, `422 validation_error`.

## Interactions

### `POST /jokes/:id/vote`

Header: bearer access token required.

Request:

```json
{ "value": 1 }
```

Rules:

- Allowed values: `1`, `-1`, `5`
- Sending the same value again removes the vote and returns `viewerVote: null`
- Sending a different value replaces the previous vote

Success `200`:

```json
{
  "score": 4,
  "viewerVote": 1
}
```

Errors: `401 unauthorized`, `404 not_found`, `422 validation_error`.

### `POST /jokes/:id/favorite`

Header: bearer access token required.

No request body. Toggle semantics.

Success `200`:

```json
{
  "isFavorited": true,
  "favoriteCount": 3
}
```

Errors: `401 unauthorized`, `404 not_found`.

### `POST /jokes/:id/report`

Header: bearer access token required.

Request:

```json
{
  "reason": "spam",
  "details": "optional details"
}
```

Rules:

- `reason`: required, trimmed, `1..50` chars
- `details`: optional, trimmed, max `500` chars

Success `200`:

```json
{ "ok": true }
```

Errors: `401 unauthorized`, `404 not_found`, `422 validation_error`.

## Profile

### `GET /profile/:username`

Auth optional.

Success `200`:

```json
{
  "user": {
    "id": "uuid",
    "username": "pointenpaule",
    "bio": null,
    "createdAt": "2026-04-28T00:00:00.000Z"
  },
  "stats": {
    "jokeCount": 1,
    "totalScore": 0,
    "averageScore": 0,
    "favoriteCategory": "wortwitze"
  },
  "jokes": [],
  "favorites": []
}
```

`jokes` and `favorites` contain `JokeItem` objects.

Errors: `404 not_found`.

## Joke Create

The backend currently exposes `POST /jokes`, but iOS MVP freeze excluded joke posting from first release. Android should treat create as disabled unless product explicitly unfreezes it.

If enabled later:

- Header: bearer access token required
- Request:

```json
{
  "content": "Warum ...?",
  "category": "wortwitze"
}
```

- `content`: required, trimmed, `1..280` chars
- `category`: backend category value
- Success `201`: created `JokeItem`
- Errors: `401 unauthorized`, `422 validation_error`, `500 server_error`

## Android QA Smoke Data

Use throwaway accounts with unique timestamps:

```json
{
  "username": "android_smoke_20260428",
  "email": "android_smoke_20260428@example.com",
  "password": "secret123"
}
```

Minimum smoke path for `BRO-163`:

1. Register, call `me`, refresh, then logout.
2. Login again and retain the new token pair.
3. Load feed with `sort=latest&category=all&cursor=0&limit=20&period=all`.
4. Open one joke detail and comments.
5. Vote `1`, vote `1` again to undo, favorite twice to toggle on/off.
6. Post one comment on a non-owned active joke.
7. Open leaderboard `scope=jokes&period=all&limit=20`.
8. Open the author's profile.

## Backend Parity Result

No Android-only backend layer is required for MVP parity. Existing mobile routes cover Android's required Auth, Feed, Random, Ranking, Profile, Vote/Superlike/Undo, Favorite, Comment and optional Create contracts.

Known caveat: create exists server-side but remains product-disabled for the first mobile release according to the iOS MVP freeze.
