# SSH Backend — Manual API / Swagger Walkthrough

This is the hand-run acceptance script for Basket 1 (application, tier, completion,
verification, payment, event, ledger) and Basket 2 (public hash-chained ledger,
revision/resubmit, funder wallet and admin sandbox payout).

Use Swagger UI for every call. Nothing here needs a test script.

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- Backend port: `8080`
- The public ledger endpoints need **no token**.
- All other endpoints need a JWT from register/login.

---

## 1. Before you start

The backend needs PostgreSQL and Redis. In this project they usually run in Docker:

```powershell
docker ps
# expect containers named ssh-postgres and ssh-redis
```

Start the backend (Java 17):

```powershell
mvn spring-boot:run
# or, after packaging:
java -jar target\ssh-backend-0.0.1-SNAPSHOT.jar
```

Wait for `Started SshApplication` and open Swagger UI. If port 8080 is already
serving `/v3/api-docs`, the backend is up.

> The database is intentionally left with zero tasks, applications, experiences,
> payments and ledger entries. The reserve wallet is seeded at **R100,000.00** the
> first time it is read. Existing non-sandbox users are left untouched. Use fresh
> sandbox emails below.

---

## 2. Authenticate in Swagger

1. Open `POST /api/v1/auth/register`.
2. Click **Try it out**, paste a registration body, and click **Execute**.
3. The response has the shape below. Copy `data.token` (the long `eyJ...` value).
4. Click the **Authorize** button at the top right of Swagger.
5. Paste the **raw token** (do not type `Bearer `; Swagger adds the scheme).
6. Click **Authorize**, then **Close**.
7. To act as a different persona, click **Authorize** → **Logout**, then paste the
   next persona's token. Swagger holds one token at a time.

Successful registration returns **HTTP 201**:

```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 101,
      "name": "Sandbox SME",
      "email": "sandbox.sme+20261024@example.com",
      "role": "SME",
      "isActive": true,
      "createdAt": "2026-10-24T17:00:00"
    }
  },
  "errors": null,
  "timestamp": "2026-10-24T17:00:00"
}
```

`POST /api/v1/auth/login` returns the same `data.token` shape for an existing user.

### Create four sandbox personas

Use a unique suffix (for example a timestamp) so a repeat run does not hit
duplicate-email 409s. Keep a notepad with each `user.id` and token.

**SME** — `POST /api/v1/auth/register`

```json
{
  "name": "Sandbox SME",
  "email": "sandbox.sme+20261024@example.com",
  "password": "Sandbox123!",
  "role": "SME",
  "businessName": "Sandbox Studio",
  "registrationNumber": "2026/SANDBOX",
  "industry": "Digital services"
}
```

**Youth** — `POST /api/v1/auth/register`

```json
{
  "name": "Sandbox Youth",
  "email": "sandbox.youth+20261024@example.com",
  "password": "Sandbox123!",
  "role": "YOUTH"
}
```

**Corporate funder** — `POST /api/v1/auth/register`

```json
{
  "name": "Sandbox Funder",
  "email": "sandbox.funder+20261024@example.com",
  "password": "Sandbox123!",
  "role": "CORPORATE"
}
```

**Admin** — `POST /api/v1/auth/register`

```json
{
  "name": "Sandbox Admin",
  "email": "sandbox.admin+20261024@example.com",
  "password": "Sandbox123!",
  "role": "ADMIN"
}
```

> Public `ADMIN` registration exists for this local sandbox only. Never expose it
> in a production deployment.

### Two different youth IDs

The youth login response `data.user.id` is the **users** table ID. Task assignment,
experience, payment and ledger APIs use the **youth profile ID**.

While authenticated as the youth, call:

`GET /api/v1/youth/me`

```json
{
  "success": true,
  "message": "Youth profile",
  "data": {
    "id": 134,
    "userId": 372,
    "userName": "Sandbox Youth",
    "isAlumni": false,
    "...": "..."
  }
}
```

Use **`data.id`** (134 above) as `{youthId}` for assign and experience lookups.
Never use `data.userId` for those calls.

---

## 3. Tier and reservation rules

The tier is derived **only** from `VERIFIED` experiences. Pending, rejected or
self-reported work never increases a tier.

| Tier | Verified experiences | Max active reservations |
|---|---|---|
| `TIER_0` | 0 | 1 |
| `TIER_1` | 1–2 | 2 |
| `TIER_2` | 3–4 | 3 |
| `TIER_3` | 5+ | unlimited |

An **active reservation** is:

- a `PENDING` application whose 48-hour reservation window has not expired, plus
- an `IN_PROGRESS` task assigned to the youth.

An `ACCEPTED` application is **not** counted a second time; the related
`IN_PROGRESS` task is the single reservation. The ceiling is checked on both
**apply** and **assign**, so an old application cannot bypass it.

After 48 hours a stale `PENDING` application is marked `EXPIRED` and releases the
slot. The youth can then apply again, which reactivates the same application row.

Check the current values while authenticated as the youth:

`GET /api/v1/tiers/me`

```json
{
  "success": true,
  "data": {
    "tier": "TIER_0",
    "verifiedExperienceCount": 0,
    "minVerifiedExperiences": 0,
    "maxVerifiedExperiences": 0,
    "activeReservations": 0,
    "maxConcurrentReservations": 1,
    "reservationWindowHours": 48,
    "canApply": true,
    "checkedAt": "2026-10-24T17:01:00"
  }
}
```

---

## 4. Happy path: apply → assign → complete → verify

### Act 1 — SME posts a task

Authenticate as the **SME** and call `POST /api/v1/tasks`.

```json
{
  "title": "Reconcile March supplier invoices",
  "description": "Match 40 supplier invoices to the purchase ledger, flag price or quantity differences, and deliver the reconciled workbook with a short exception note.",
  "category": "Accounting & Finance",
  "budget": 1200,
  "durationDays": 5,
  "isPaid": true
}
```

Expected **HTTP 201**. `data` is the task:

```json
{
  "success": true,
  "message": "Task created",
  "data": {
    "id": 17,
    "title": "Reconcile March supplier invoices",
    "status": "OPEN",
    "budget": 1200.0,
    "durationDays": 5,
    "isPaid": true,
    "smeBusinessName": "Sandbox Studio",
    "assignedToId": null,
    "revisionReason": null,
    "revisionCount": 0,
    "createdAt": "2026-10-24T17:02:00"
  }
}
```

Copy `data.id` as **`{taskId}`**. Confirm `isPaid` is the field name (not `paid`).

### Act 2 — Youth checks eligibility and applies

Authenticate as the **youth**.

1. `GET /api/v1/youth/me` → copy `data.id` as **`{youthId}`**.
2. `GET /api/v1/tiers/me` → expect `TIER_0`, `activeReservations: 0`,
   `maxConcurrentReservations: 1`, `canApply: true`.
3. Apply to the task:

`POST /api/v1/tasks/{taskId}/apply`

```json
{
  "coverNote": "I have reconciled supplier ledgers for two retail businesses and can start immediately."
}
```

Expected **HTTP 201**:

```json
{
  "success": true,
  "message": "Application submitted",
  "data": {
    "id": 8,
    "taskId": 17,
    "taskTitle": "Reconcile March supplier invoices",
    "youthId": 134,
    "youthName": "Sandbox Youth",
    "status": "PENDING",
    "coverNote": "I have reconciled supplier ledgers...",
    "appliedAt": "2026-10-24T17:03:00",
    "expiresAt": "2026-10-26T17:03:00",
    "decidedAt": null
  }
}
```

Copy the application `id` if you want to test withdrawal.

4. **Duplicate check:** execute the same apply call again. Expected **HTTP 409**:

```json
{
  "success": false,
  "message": "An application for this task is already pending",
  "data": null,
  "errors": null,
  "timestamp": "2026-10-24T17:03:05"
}
```

No second reservation is created.

5. `GET /api/v1/tiers/me` → now `activeReservations: 1`, `canApply: false`.

6. **Tier-0 ceiling check:** while that application/task is active, create a second
   open task as the SME and try to apply as the same youth. Expected **HTTP 409**
   with a message such as:

```text
Tier TIER_0 allows at most 1 active reservation(s); finish verified work to unlock more
```

At Tier 0 the youth cannot reserve a second task until task A is verified.

### Act 3 — SME reviews applicants and assigns

Authenticate as the **SME**.

1. `GET /api/v1/tasks/{taskId}/applications`

```json
{
  "success": true,
  "data": [
    {
      "id": 8,
      "taskId": 17,
      "youthId": 134,
      "youthName": "Sandbox Youth",
      "status": "PENDING",
      "appliedAt": "2026-10-24T17:03:00",
      "expiresAt": "2026-10-26T17:03:00"
    }
  ]
}
```

2. Assign the applicant:

`PATCH /api/v1/tasks/{taskId}/assign?youthId=134`

No request body. Swagger shows `youthId` as a query parameter.

Expected **HTTP 200**:

```json
{
  "success": true,
  "message": "Task assigned",
  "data": {
    "id": 17,
    "status": "IN_PROGRESS",
    "assignedToId": 134,
    "assignedToName": "Sandbox Youth",
    "revisionCount": 0
  }
}
```

Assigning accepts the chosen application and rejects the other pending applicants.
The task must have a live `PENDING` application; assigning a youth who never
applied returns **409**.

3. `GET /api/v1/tasks/{taskId}/applications` again → the chosen application is
   `ACCEPTED`; any other pending applicants are `REJECTED`.

### Act 4 — Youth completes the work

Authenticate as the **youth**.

`PATCH /api/v1/tasks/{taskId}/complete`

No body. Expected **HTTP 200** with `data.status: "COMPLETED"`.

Behind the scenes exactly one `Experience` row is created for the task, linked to
the task and the assigned youth, with status `PENDING`. You can see it with:

`GET /api/v1/experiences/youth/{youthId}`

Only the assigned youth can complete the task. A different youth gets **403**.

### Act 5 — SME verifies, creating the experience, payment and ledger entry

Authenticate as the **SME**.

`PATCH /api/v1/tasks/{taskId}/verify`

```json
{
  "rating": 5,
  "feedback": "The workbook reconciled all 40 invoices, every exception was explained, and the file arrived a day early.",
  "reference": "Evidence: reconciled workbook FIN-MAR-2026.xlsx in /Finance/2026/03"
}
```

All three fields are mandatory:

- `rating`: 1.0–5.0
- `feedback`: non-blank, max 1000 characters
- `reference`: non-blank, max 255 characters, describing the evidence checked

Expected **HTTP 200**:

```json
{
  "success": true,
  "message": "Task verified",
  "data": {
    "task": {
      "id": 17,
      "status": "VERIFIED",
      "assignedToId": 134,
      "revisionCount": 0
    },
    "experience": {
      "id": 91,
      "status": "VERIFIED",
      "isVerified": true,
      "rating": 5.0,
      "feedback": "The workbook reconciled all 40 invoices...",
      "reference": "Evidence: reconciled workbook FIN-MAR-2026.xlsx in /Finance/2026/03",
      "taskId": 17,
      "youthId": 134,
      "verifiedAt": "2026-10-24T17:05:00.123"
    },
    "payment": {
      "id": 91,
      "taskId": 17,
      "taskTitle": "Reconcile March supplier invoices",
      "youthId": 134,
      "totalAmount": 1200.0,
      "platformFee": 120.0,
      "youthAmount": 1080.0,
      "status": "PENDING",
      "reference": null,
      "paidAt": null
    }
  }
}
```

What this single call guarantees:

- Task status becomes `VERIFIED`.
- The same `Experience` row becomes `VERIFIED` and `isVerified: true`.
- Exactly one `Payment` is created for the task, with status `PENDING`.
- `totalAmount` = task budget (`0` if no budget was set).
- `platformFee` = 10%, rounded to cents.
- `youthAmount` = remainder.
- A hash-chained public ledger entry is appended.
- A verification/payment event is published; an `AuditLog` row is written after commit.

Copy `data.experience.id` and `data.payment.id` for the later checks.

**Duplicate verification:** repeat the same verify call. Expected **HTTP 409**
(`Task 17 cannot move from VERIFIED to VERIFIED`). No second experience, payment or
ledger entry is created.

The same verification can also be invoked by experience ID:

`PATCH /api/v1/experiences/{experienceId}/verify`

with the identical body. It is an alias of task verification.

---

## 5. Public ledger proof (no authentication)

These GETs are public. You may leave the Authorize token in place or log out.

1. `GET /api/v1/ledger/entries`

```json
{
  "success": true,
  "message": "Public ledger",
  "data": [
    {
      "id": 3,
      "sequence": 1,
      "experienceId": 91,
      "taskId": 17,
      "youthId": 134,
      "rating": 5.0,
      "feedback": "The workbook reconciled all 40 invoices...",
      "reference": "Evidence: reconciled workbook FIN-MAR-2026.xlsx in /Finance/2026/03",
      "verifiedAt": "2026-10-24T17:05:00.123",
      "previousHash": "GENESIS",
      "hash": "79344d48391be5b45b802c4e8945d868fc3fc10b463378566c36dd570268d15a"
    }
  ]
}
```

2. `GET /api/v1/ledger/verify`

```json
{
  "success": true,
  "message": "Ledger verification",
  "data": {
    "valid": true,
    "checkedEntries": 1,
    "brokenAtSequence": null,
    "message": "Ledger chain is intact (1 entries verified)"
  }
}
```

3. `GET /api/v1/ledger/verify/{experienceId}` — verifies the chain from genesis up
   to that experience's entry. `valid: true`; `checkedEntries` is the number of
   entries walked.

4. `GET /api/v1/ledger/entries/{id}` — one entry by its ledger ID.

The hash is SHA-256 over this canonical payload:

```text
sequence|experienceId|taskId|youthId|rating|feedback|reference|verifiedAt|previousHash
```

The first entry chains from `GENESIS`; each later entry stores the previous entry's
hash. Changing any stored field breaks verification, and `/verify` reports the
first broken sequence. Run `/verify` again after adding more verified tasks: it
should stay `valid: true` with a higher `checkedEntries`.

Unknown experience IDs return **404** (`No ledger entry for experience ...`).

---

## 6. Payments, funder reserve and sandbox payout

### Youth payments

Authenticate as the **youth** and call `GET /api/v1/payments/mine`.

Expected one `PENDING` payment with the 10% split. The youth may also call
`GET /api/v1/payments/{paymentId}`; the owning SME and admins may read it too.

### Corporate funder tops up the reserve

Authenticate as the **CORPORATE** user.

1. `GET /api/v1/wallets/reserve`

```json
{
  "success": true,
  "data": {
    "id": 3,
    "name": "SSH_PLATFORM_RESERVE",
    "balance": 100000.0,
    "currency": "ZAR",
    "updatedAt": "2026-10-24T17:00:00"
  }
}
```

The wallet is seeded once at **R100,000.00**. Its balance may include earlier
sandbox activity; it is not a production bank account.

2. `POST /api/v1/wallets/reserve/fund`

```json
{
  "amount": 25000,
  "reference": "CORP-PLEDGE-2026-001"
}
```

Expected **HTTP 200**; balance becomes `125000.0`. A `CREDIT` transaction is
recorded.

3. `GET /api/v1/wallets/reserve/transactions`

```json
{
  "success": true,
  "data": [
    {
      "id": 12,
      "type": "CREDIT",
      "amount": 25000.0,
      "balanceAfter": 125000.0,
      "reference": "CORP-PLEDGE-2026-001",
      "createdAt": "2026-10-24T17:10:00"
    }
  ]
}
```

### Admin runs the sandbox payout

Authenticate as the **ADMIN**.

`POST /api/v1/payments/{paymentId}/payout`

No request body. Expected **HTTP 200**:

```json
{
  "success": true,
  "message": "Payment paid (sandbox)",
  "data": {
    "id": 91,
    "status": "PAID",
    "reference": "SANDBOX-8f9ae74e-50ca-48fe-bcd1-7c1d0e9f9a11",
    "paidAt": "2026-10-24T17:12:00",
    "youthAmount": 1080.0
  }
}
```

The reserve is debited by `youthAmount` (1080), so the balance becomes `123920.0`.
A `DEBIT` transaction is recorded with reference `Payout for payment 91`.

Checks:

- `GET /api/v1/wallets/reserve` → balance decreased by exactly `youthAmount`.
- `GET /api/v1/wallets/reserve/transactions` → a second row, `type: "DEBIT"`.
- Repeat the payout → **HTTP 409** (`Payment 91 has already been paid`).
- Call payout as CORPORATE or YOUTH → **HTTP 403**.
- If the reserve has insufficient funds, payout → **HTTP 409** and no payment is marked paid.

SMEs never handle money in this flow. Verification creates the payment; only an
admin releases it.

---

## 7. Revision and resubmit

This proves the `COMPLETED → IN_PROGRESS → COMPLETED → VERIFIED` loop does not
create duplicate experiences or payments.

1. As the **SME**, create a second task (budget `500`) and copy its `{taskBId}`.
2. As the **youth**, `POST /api/v1/tasks/{taskBId}/apply` with a cover note.
   After one verified experience the youth is `TIER_1` (max 2 reservations), so
   this is allowed.
3. As the **SME**, assign the applicant with
   `PATCH /api/v1/tasks/{taskBId}/assign?youthId={youthId}`.
4. As the **youth**, `PATCH /api/v1/tasks/{taskBId}/complete`.
5. As the **SME**, send it back:

`PATCH /api/v1/tasks/{taskBId}/reject`

```json
{
  "reason": "Please attach the signed handover note and correct the two invoice totals."
}
```

Expected **HTTP 200**:

```json
{
  "success": true,
  "message": "Task sent back for revision",
  "data": {
    "id": 18,
    "status": "IN_PROGRESS",
    "revisionReason": "Please attach the signed handover note and correct the two invoice totals.",
    "revisionCount": 1
  }
}
```

The task returns to `IN_PROGRESS` for the same youth. The existing experience is
marked `REJECTED` and stores `rejectionReason`; **no payment** is created yet.

6. As the **youth**, `PATCH /api/v1/tasks/{taskBId}/complete` again.
7. As the **SME**, `PATCH /api/v1/tasks/{taskBId}/verify` with a new rating,
   feedback and reference.

Expected **HTTP 200**: task `VERIFIED`, the **same** experience row now `VERIFIED`,
one new `PENDING` payment, and one new ledger entry. `revisionCount` stays `1`.
There must not be a second experience for task B.

Rejecting a task that is not `COMPLETED`, or a task owned by another SME, returns
**403/409** as appropriate.

---

## 8. Negative checks

Run these after the happy path. Switch tokens as noted.

| Check | Call | Expected |
|---|---|---|
| No token on a protected endpoint | `GET /api/v1/tasks/mine` without Authorize | **401** |
| Wrong role | Youth calls `POST /api/v1/tasks` | **403** |
| Non-owning SME assigns/verifies | Second SME uses the first SME's task | **403** |
| Assign with no application | `PATCH /api/v1/tasks/{id}/assign?youthId={youthId}` before applying | **409** |
| Duplicate pending application | Apply twice to the same open task | **409** |
| Tier ceiling | Tier-0 youth applies for a second task while the first is active | **409** |
| Verify before completion | Verify an `OPEN` or `IN_PROGRESS` task | **409** |
| Missing reference | Verify with only rating and feedback | **400** |
| Rating out of range | `rating: 0` or `rating: 6` | **400** |
| Blank feedback/reference | `""` or whitespace | **400** |
| Duplicate verification | Verify an already `VERIFIED` task | **409** |
| Duplicate payout | Payout an already `PAID` payment | **409** |
| Insufficient reserve | Payout when reserve < youth amount | **409** |
| Unknown resource | Unknown task/experience/payment/ledger ID | **404** |

For the validation cases, the response is the standard error envelope with a
`message`; field-level errors appear in `errors` when Bean Validation rejects the
body.

---

## 9. Error envelope and status codes

Every error handled by the API uses the same envelope:

```json
{
  "success": false,
  "message": "An application for this task is already pending",
  "data": null,
  "errors": null,
  "timestamp": "2026-10-24T17:14:48.9830889"
}
```

Status meanings:

| Status | Meaning |
|---|---|
| `200 OK` | Read/state transition succeeded |
| `201 Created` | Registration, task creation or application creation succeeded |
| `400 Bad Request` | Bean Validation or malformed JSON |
| `401 Unauthorized` | Missing/invalid JWT, or bad login credentials |
| `403 Forbidden` | Valid JWT but wrong role or not the resource owner |
| `404 Not Found` | Task/experience/payment/ledger entry does not exist |
| `409 Conflict` | Duplicate application, tier ceiling, invalid task transition, duplicate verification, already-paid payment, insufficient reserve |

`409` is used for business-rule conflicts, not `401`. If an older client treats
every non-2xx response as an auth failure, check the body's `message` first.

---

## 10. Field notes and gotchas

- **`youthId` is the youth profile ID**, not `users.id`. Resolve it with
  `GET /api/v1/youth/me` → `data.id`.
- **`isPaid`, `isVerified`, `isAlumni` and `isActive` are the JSON field names.**
  They are kept stable with `@JsonProperty`; do not switch to `paid`, `verified`,
  `alumni` or `active` in client code. Task creation also accepts the legacy
  `paid` alias.
- **`reference` is mandatory on verification.** Use a real file name, URL, delivery
  note or shared-drive reference. It is sealed into the public ledger.
- **Ratings** are `1.0`–`5.0`; `0` is rejected.
- **Reservations last 48 hours** from application creation. Expired applications
  become `EXPIRED` and free the slot.
- **Application statuses:** `PENDING`, `ACCEPTED`, `REJECTED`, `WITHDRAWN`,
  `EXPIRED`. A rejected/withdrawn/expired application can be reactivated by
  applying again; a live `PENDING`/`ACCEPTED` application cannot.
- **Task graph:** `OPEN → IN_PROGRESS → COMPLETED → VERIFIED`. Rejection moves
  `COMPLETED → IN_PROGRESS`. Invalid transitions return `409`.
- **One experience per task** (`experiences.task_id` is unique). Rejection reuses
  the row and clears it when the work is re-completed; verification reuses it again.
- **One payment per verified task.** The payment stays `PENDING` until an admin
  sandbox payout marks it `PAID`.
- **Payment split:** `totalAmount` = task budget (or 0), `platformFee` = 10% rounded
  to cents, `youthAmount` = `totalAmount - platformFee`.
- **The public ledger is append-only.** Every verification adds one entry. Do not
  edit `ledger_entries` rows by hand; the chain check will report the modified
  sequence.
- **No real money moves.** The reserve wallet and `SANDBOX-<uuid>` payout references
  are demo-only.

---

## 11. Quick UI smoke check (optional)

With the backend running:

```powershell
cd frontend
npm run dev
```

1. Log in as the youth. The dashboard shows the tier badge, active reservations,
   verified experiences and total earned from payments.
2. Open a task, apply, and confirm the pending/reserved state. Apply again is
   prevented; withdraw releases the reservation.
3. Log in as the SME. The dashboard lists only that SME's tasks. Open an `OPEN`
   task, review the applicant dropdown, and assign.
4. As the youth, mark the assigned task completed.
5. As the SME, verify with a 1–5 rating, feedback and reference. The task becomes
   `VERIFIED`; the UI shows the ledger confirmation and payment split.
6. Use **Send Back for Revision** on a completed task, then complete and verify
   again to exercise the revision loop.
7. Check the public ledger endpoints in Swagger or a browser:
   <http://localhost:8080/api/v1/ledger/verify>.

The frontend uses the same JWT and the youth profile ID described above; it does
not call any hidden endpoints.

---

## 12. What "done" looks like

After one full happy path you should have:

- one task in `VERIFIED`,
- one application in `ACCEPTED`,
- one experience in `VERIFIED` with rating, feedback and reference,
- one payment in `PENDING` (or `PAID` after the admin payout) with a correct split,
- ledger `/verify` returning `valid: true`,
- a `VERIFY` audit row in `audit_logs` (written after commit), plus a `PAY` audit\n  row after the sandbox payout,
- a `CREDIT` funding transaction and, after payout, a `DEBIT` transaction,
- reserve balance reduced by the youth amount after payout.

That is the complete Basket 1 + Basket 2 acceptance path.
