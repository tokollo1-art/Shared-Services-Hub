# Shared Services Hub (SSH) — Detailed GitHub Issues Backlog

This backlog provides comprehensive, production-grade GitHub issue specifications. Each issue includes an Agile User Story, technical problem analysis, schema designs, API contracts, step-by-step implementation tasks, BDD (Given-When-Then) acceptance criteria, and edge cases.

---

## Milestone 1: Core Lifecycle & Integration Fixes

### Issue #1: `fix(experience)`: Auto-create pending Experience on task completion & fix SME verification taskId mismatch

- **Milestone:** Milestone 1 — Core Lifecycle & Integration Fixes
- **Labels:** `bug`, `backend`, `frontend`, `p0-critical`, `experience-ledger`
- **Priority:** P0 (Blocker)

#### User Story
```gherkin
As a Youth Candidate who has completed an assigned task,
I want the system to automatically generate a pending Experience Ledger entry upon completion,
So that my SME enterprise partner can review, rate, and verify my work without manual ledger creation or API failures.

As an SME Partner,
I want to verify a completed task from my dashboard using the correct Experience Ledger identifier,
So that the candidate's work record is permanently and reliably recorded into their Experience Ledger.
```

#### Problem Analysis
1. In [`TaskService.completeTask(Long taskId)`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/service/TaskService.java#L138-L145), the code sets `task.setStatus("COMPLETED")` and saves the task. It never calls [`ExperienceService.createExperience(taskId)`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/service/ExperienceService.java#L52-L67), leaving the task with no corresponding [`Experience`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/entity/Experience.java) row.
2. In [`frontend/src/routes/dashboard/sme/+page.svelte`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/frontend/src/routes/dashboard/sme/+page.svelte#L135) and [`frontend/src/routes/tasks/[id]/+page.svelte`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/frontend/src/routes/tasks/%5Bid%5D/+page.svelte#L94), the verification submission executes:
   ```javascript
   const experienceId = selectedTaskToVerify.id; // Bug: passing taskId as experienceId
   await api.experiences.verify(experienceId, verifyFeedback, verifyRating);
   ```
   Because `Task.id` does not equal `Experience.id`, the backend throws a `ResourceNotFoundException("Experience", id)`.

#### Technical Specification
1. **Backend Service Integration:**
   - Inject [`ExperienceService`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/service/ExperienceService.java) into [`TaskService`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/service/TaskService.java).
   - In [`TaskService.completeTask()`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/service/TaskService.java#L138):
     ```java
     task.setStatus("COMPLETED");
     Task saved = taskRepository.save(task);
     ExperienceResponse exp = experienceService.createExperience(saved.getId());
     ```
   - Update [`TaskResponse`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/dto/response/TaskResponse.java) DTO:
     - Add `private Long experienceId;`
     - Map `experienceId` in `toTaskResponse()`.
   - Update [`ExperienceController`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/controller/ExperienceController.java):
     - Add endpoint `GET /api/v1/experiences/task/{taskId}` returning `ApiResponse<ExperienceResponse>`.
2. **Frontend Fixes:**
   - In [`dashboard/sme/+page.svelte`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/frontend/src/routes/dashboard/sme/+page.svelte) and [`tasks/[id]/+page.svelte`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/frontend/src/routes/tasks/%5Bid%5D/+page.svelte):
     - Check `task.experienceId`. If absent, fetch experience via `api.experiences.getByTaskId(task.id)`.
     - Pass the valid `experienceId` to `api.experiences.verify(experienceId, feedback, rating)`.
     - On successful verification, reload both task status and experience ledger.

#### Implementation Tasks
- [ ] Inject `ExperienceService` into `TaskService`.
- [ ] Update `TaskService.completeTask` to create the experience record idempotently (ensure no duplicate experience is created if already existing).
- [ ] Add `experienceId` field and getter/setter to `TaskResponse`.
- [ ] Add `findByTaskId(Long taskId)` to `ExperienceRepository`.
- [ ] Add `GET /api/v1/experiences/task/{taskId}` endpoint in `ExperienceController`.
- [ ] Update frontend SME verification modal logic in `dashboard/sme/+page.svelte` and `tasks/[id]/+page.svelte`.
- [ ] Write integration test `shouldCreateExperienceRecord_WhenTaskMarkedCompleted()`.
- [ ] Write integration test `shouldVerifyExperience_WhenValidExperienceIdProvided()`.

#### BDD Acceptance Criteria
```gherkin
Scenario: Youth completes a task successfully
  Given a youth candidate is assigned to a task with status "IN_PROGRESS"
  When the youth clicks "Mark as Completed"
  Then the task status becomes "COMPLETED"
  And a new Experience record is persisted with status "PENDING_VERIFICATION" and isVerified false
  And the TaskResponse payload includes the generated experienceId

Scenario: SME verifies completed work
  Given a task with status "COMPLETED" and an associated pending Experience record
  When the SME submits a 5-star rating and written feedback
  Then the Experience record has isVerified set to true, rating set to 5.0, and verifiedAt populated
  And the Task status transitions to "VERIFIED"
  And the Youth's Experience Ledger immediately reflects the new verified entry
```

---

### Issue #2: `feat(task)`: Implement Youth Task Application & SME Applicant Review Flow

- **Milestone:** Milestone 1 — Core Lifecycle & Integration Fixes
- **Labels:** `enhancement`, `backend`, `frontend`, `tasks`, `workflow`
- **Priority:** P0 (Core Flow)

#### User Story
```gherkin
As a Youth Candidate,
I want to submit an application with an introductory pitch for an open task,
So that the SME can review my profile, capability, and interest before deciding to assign the task to me.

As an SME Partner,
I want to review all candidate applications for my posted tasks,
So that I can select the best-suited youth candidate rather than having tasks claimed blindly without my vetting.
```

#### Problem Analysis
- Currently, [`frontend/src/routes/tasks/[id]/+page.svelte`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/frontend/src/routes/tasks/%5Bid%5D/+page.svelte#L61-L73) directly assigns the task to the current user via `api.tasks.assign(task.id, user.id)`.
- It passes `user.id` instead of `Youth.id`, causing data inconsistency.
- There is no model or UI for candidates to apply or for SMEs to review and compare applicants.

#### Technical Specification
1. **Database & Entity Model:**
   - Create entity `TaskApplication` (`table: task_applications`):
     ```sql
     CREATE TABLE task_applications (
       id BIGSERIAL PRIMARY KEY,
       task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
       youth_id BIGINT NOT NULL REFERENCES youth(id) ON DELETE CASCADE,
       pitch TEXT,
       status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, ACCEPTED, REJECTED, WITHDRAWN
       created_at TIMESTAMP NOT NULL,
       updated_at TIMESTAMP NOT NULL,
       CONSTRAINT unique_task_youth_application UNIQUE (task_id, youth_id)
     );
     ```
2. **DTOs:**
   - `TaskApplicationRequest`: `pitch: String (max 1000)`.
   - `TaskApplicationResponse`: `id`, `taskId`, `taskTitle`, `youthId`, `youthName`, `youthSkills`, `youthRating`, `pitch`, `status`, `createdAt`.
3. **API Contracts:**
   - `POST /api/v1/tasks/{id}/apply`: Authenticated as `ROLE_YOUTH`. Creates application in `PENDING` status.
   - `GET /api/v1/tasks/{id}/applications`: Authenticated as `ROLE_SME` (must own task) or `ROLE_ADMIN`. Returns list of applications.
   - `PATCH /api/v1/tasks/{taskId}/applications/{appId}/accept`: SME accepts applicant. Automatically assigns task to candidate, transitions task to `IN_PROGRESS`, marks chosen application `ACCEPTED`, and updates remaining applications to `REJECTED`.
   - `GET /api/v1/youth/me/applications`: Youth views all their pending/accepted applications.
4. **Frontend Components:**
   - `TaskDetail` (`tasks/[id]/+page.svelte`): Replace instant "Claim" button with "Apply for Task" modal with a pitch textarea. If already applied, show "Application Submitted — Awaiting SME Review".
   - `SmeDashboard` (`dashboard/sme/+page.svelte`): Add "View Applicants ({count})" badge on open tasks opening an applicant review drawer.

#### Implementation Tasks
- [ ] Create `TaskApplication` entity and `TaskApplicationRepository`.
- [ ] Implement `applyForTask()` in `TaskService` with validation (task must be `OPEN`, user must have `Youth` profile, cannot apply twice).
- [ ] Implement `getApplicationsForTask()` and `acceptApplication()` in `TaskService`.
- [ ] Add endpoints to `TaskController`.
- [ ] Build Application Modal in `frontend/src/routes/tasks/[id]/+page.svelte`.
- [ ] Build Applicant Review Drawer in `frontend/src/routes/dashboard/sme/+page.svelte`.
- [ ] Unit & Integration tests for application lifecycle.

#### BDD Acceptance Criteria
```gherkin
Scenario: Youth submits an application
  Given a task with status "OPEN"
  And an authenticated Youth candidate who has not yet applied
  When the youth enters a 200-word pitch and submits the application
  Then a TaskApplication record is stored with status "PENDING"
  And the task status remains "OPEN"
  And the youth sees an "Application Pending" status indicator

Scenario: SME accepts an applicant
  Given an open task with 3 pending candidate applications
  When the SME reviews applicant #2 and clicks "Accept Applicant"
  Then applicant #2's application status transitions to "ACCEPTED"
  And the other 2 applications transition to "REJECTED"
  And the task assigned_to is set to youth #2
  And the task status advances to "IN_PROGRESS"
```

---

### Issue #3: `fix(api)`: Implement Centralized Global Exception Handler (`@RestControllerAdvice`)

- **Milestone:** Milestone 1 — Core Lifecycle & Integration Fixes
- **Labels:** `backend`, `security`, `dx`, `api-design`
- **Priority:** P1 (High)

#### User Story
```gherkin
As a Frontend Developer or API Consumer,
I want all backend errors and validation failures to return a predictable, standardized JSON envelope,
So that the frontend can consistently display clear error notifications without unexpected crash pages or HTML parsing errors.
```

#### Technical Specification
1. Create `com.ssh.exception.GlobalExceptionHandler` annotated with `@RestControllerAdvice`.
2. Format all error responses using [`ApiResponse<T>`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/dto/response/ApiResponse.java):
   ```json
   {
     "success": false,
     "message": "Resource not found with id: 42",
     "data": null,
     "errors": {
       "field": "Validation message"
     },
     "timestamp": "2026-10-18T12:00:00"
   }
   ```
3. Handlers to define:
   - `ResourceNotFoundException` -> 404 Not Found
   - `DuplicateResourceException` -> 409 Conflict
   - `MethodArgumentNotValidException` -> 400 Bad Request (extract each `FieldError`)
   - `BadCredentialsException` -> 401 Unauthorized ("Invalid email or password")
   - `AccessDeniedException` -> 403 Forbidden ("You do not have permission to perform this action")
   - `IllegalStateException` / `IllegalArgumentException` -> 400 Bad Request
   - Fallback `Exception` -> 500 Internal Server Error (logged with stack trace; sanitize user-facing message)

#### Implementation Tasks
- [ ] Create `GlobalExceptionHandler.java` in `com.ssh.exception`.
- [ ] Add `errors` dictionary field to `ApiResponse.java` for validation details.
- [ ] Write unit tests verifying JSON structures for each HTTP status code.

#### BDD Acceptance Criteria
```gherkin
Scenario: Invalid registration request
  When a client POSTs to "/api/v1/auth/register" with an invalid email and a 4-character password
  Then the HTTP status is 400 Bad Request
  And the response body matches the ApiResponse structure with success=false
  And the errors map specifies validation issues for "email" and "password"
```

---

## Milestone 2: Payment & Payouts Engine (Fintech Guarantee)

### Issue #4: `feat(payment)`: Implement `PaymentService`, `PaymentController`, and Automated Payout Splits

- **Milestone:** Milestone 2 — Payment & Payouts Engine
- **Labels:** `backend`, `feature`, `fintech`, `p0-critical`
- **Priority:** P0 (Core Value Proposition)

#### User Story
```gherkin
As an SSH Platform Operator,
I want the platform to automatically calculate payment splits and record a youth wage payout when an SME verifies a task,
So that corporate funds are disbursed directly to youth without SMEs handling money or candidates facing payroll exploitation.
```

#### Problem Analysis
- [`README.md`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/README.md#L104-L114) specifies `Payment Controller` and `Payment Service`.
- Entity [`Payment.java`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/entity/Payment.java), [`PaymentRepository.java`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/repository/PaymentRepository.java), and [`PaymentRequest.java`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/dto/request/PaymentRequest.java) exist, but no service or controller was ever created.
- The platform currently has zero payment processing capability.

#### Technical Specification
1. **DTOs:**
   - Create `PaymentResponse`: `id`, `taskId`, `taskTitle`, `youthId`, `youthName`, `totalAmount`, `platformFee`, `youthAmount`, `status`, `reference`, `paidAt`, `createdAt`.
2. **Service Layer (`PaymentService.java`):**
   - `createTaskPayment(Task task, Youth youth)`: Calculates split (default: 90% youthAmount, 10% platformFee). Persists `Payment` with status `PENDING`.
   - `disbursePayment(Long paymentId)`: Simulates / executes payout, generates unique reference `SSH-PAY-{UUID}`, sets `paidAt = LocalDateTime.now()`, and transitions status to `PAID`.
   - `getPaymentsByYouth(Long youthId)`: Fetches payout history.
   - `getPaymentByTask(Long taskId)`.
   - `getTotalDisbursedByCorporate(Long corporateId)`.
3. **Controller Layer (`PaymentController.java`):**
   - `GET /api/v1/payments/youth/{youthId}` (authenticated as matching youth or admin).
   - `GET /api/v1/payments/task/{taskId}`.
   - `POST /api/v1/payments/{id}/disburse` (Admin / Automated worker).
4. **Lifecycle Trigger:**
   - In [`ExperienceService.verifyExperience()`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/service/ExperienceService.java#L73-L88), upon SME verification, call `paymentService.createTaskPayment(task, youth)` and immediately trigger payout disbursement.

#### Implementation Tasks
- [ ] Create `PaymentResponse.java` DTO.
- [ ] Implement `PaymentService.java` with calculation and disbursement logic.
- [ ] Connect `PaymentService` to `ExperienceService.verifyExperience()`.
- [ ] Create `PaymentController.java` with Swagger `@Operation` tags and role-based security.
- [ ] Write unit tests for fee calculation (e.g. R1,000 budget = R900 youth, R100 platform fee).
- [ ] Write integration test verifying complete flow: verify experience -> generate payment -> mark paid.

#### BDD Acceptance Criteria
```gherkin
Scenario: Task verification automatically triggers youth payment
  Given a completed task with a corporate-funded budget of R1,200.00
  When the SME verifies the task
  Then a Payment entity is created with:
    | totalAmount  | platformFee | youthAmount | status |
    | 1200.00      | 120.00      | 1080.00     | PAID   |
  And a disbursement reference "SSH-PAY-..." is generated
  And the payment appears in the Youth's payment history
```

---

### Issue #5: `feat(frontend)`: Connect Youth Earnings Dashboard to Real Payments API

- **Milestone:** Milestone 2 — Payment & Payouts Engine
- **Labels:** `frontend`, `ui`, `fintech`
- **Priority:** P1 (High)

#### User Story
```gherkin
As a Youth Candidate,
I want to view my verified earnings, pending payments, and transaction history on my dashboard,
So that I have full transparency over my income and guaranteed direct payouts.
```

#### Problem Analysis
In [`frontend/src/routes/dashboard/youth/+page.svelte`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/frontend/src/routes/dashboard/youth/+page.svelte#L30-L32), total earnings are simulated as:
```javascript
const totalEarned = $derived.by(() => {
    return experiences.length * 1100;
});
```
There is no table or component showing actual payment transactions or disbursement receipts.

#### Technical Specification
1. Update [`frontend/src/lib/api.js`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/frontend/src/lib/api.js):
   - Add `api.payments.listForYouth(youthId)`.
   - Add `api.payments.get(paymentId)`.
2. Update `dashboard/youth/+page.svelte`:
   - Fetch real payments list via `api.payments.listForYouth(user.id)`.
   - Calculate total earned from `payments.filter(p => p.status === 'PAID').reduce((sum, p) => sum + p.youthAmount, 0)`.
   - Add a dedicated "Earnings & Disbursements" ledger table displaying:
     - Date Disbursed
     - Associated Task Title
     - Gross Corporate Funding
     - Net Payout Received (ZAR)
     - Payment Status Badge (`PAID`, `PROCESSING`, `PENDING`)
     - Reference Number

#### Implementation Tasks
- [ ] Add payments methods to `api.js`.
- [ ] Replace hardcoded formula in `dashboard/youth/+page.svelte`.
- [ ] Build responsive desktop/mobile transaction history table.
- [ ] Add empty state: "No payments recorded yet. Complete and verify tasks to earn income."

#### BDD Acceptance Criteria
```gherkin
Scenario: Youth candidate views earnings
  Given an authenticated youth who has 2 verified tasks yielding R900 and R1,350 net payouts
  When they visit their dashboard
  Then "Total Paid" displays "R 2,250.00"
  And the Earnings ledger lists both transactions with valid reference numbers and "PAID" status badges
```

---

## Milestone 3: Corporate & Cohort Funding (B-BBEE & ESD Engine)

### Issue #6: `feat(corporate)`: Implement Corporate Cohort Funding Backend

- **Milestone:** Milestone 3 — Corporate & Cohort Funding
- **Labels:** `backend`, `feature`, `b-bbee`, `enterprise`
- **Priority:** P1 (High)

#### User Story
```gherkin
As a Corporate Sponsor,
I want to allocate Enterprise and Supplier Development (ESD) funds into youth cohorts,
So that I can fund youth wages, empower local SMEs, and track real-time data for B-BBEE scorecard compliance.
```

#### Technical Specification
1. **Entities & Relationships:**
   - Create `Corporate` entity (`table: corporates`):
     - `id: Long (PK)`
     - `user: User (OneToOne)`
     - `companyName: String (max 150)`
     - `registrationNumber: String`
     - `bbbeeLevel: String`
     - `totalCommittedBudget: Double`
   - Create `Cohort` entity (`table: cohorts`):
     - `id: Long (PK)`
     - `corporate: Corporate (ManyToOne)`
     - `name: String`
     - `allocatedBudget: Double`
     - `remainingBudget: Double`
     - `maxCandidates: Integer`
     - `status: String (ACTIVE, DEPLETED, CLOSED)`
2. **DTOs:**
   - `CohortRequest`: `name`, `allocatedBudget`, `maxCandidates`, `focusIndustry`.
   - `CohortResponse`: `id`, `corporateName`, `name`, `allocatedBudget`, `remainingBudget`, `disbursedBudget`, `candidateCount`, `status`.
   - `CorporateImpactReportResponse`: `candidatesFunded`, `wagesDisbursed`, `smesSupported`, `bbbeeScorecardPoints`, `ledgerVerificationRate`.
3. **Controller & Service (`CorporateService`, `CorporateController`):**
   - `POST /api/v1/corporate/cohorts`
   - `GET /api/v1/corporate/cohorts`
   - `GET /api/v1/corporate/impact-report`

#### Implementation Tasks
- [ ] Create `Corporate` and `Cohort` entities with Liquibase changelog.
- [ ] Implement `CorporateService` with budget tracking.
- [ ] Create `CorporateController` secured with `@PreAuthorize("hasRole('CORPORATE') or hasRole('ADMIN')")`.
- [ ] Add automated test verifying remaining budget decreases when task wages disburse.

#### BDD Acceptance Criteria
```gherkin
Scenario: Corporate funds a cohort
  Given an authenticated Corporate sponsor
  When they POST to "/api/v1/corporate/cohorts" with budget R100,000 and capacity 20
  Then a new Cohort is created in ACTIVE status with remainingBudget R100,000
  And the impact metrics report updates the total committed capital
```

---

### Issue #7: `feat(frontend)`: Build Interactive Corporate Funding Flow & B-BBEE Audit Pack Export

- **Milestone:** Milestone 3 — Corporate & Cohort Funding
- **Labels:** `frontend`, `ui`, `b-bbee`
- **Priority:** P1 (High)

#### User Story
```gherkin
As a Corporate ESG / B-BBEE Compliance Manager,
I want to initiate cohort funding commitments and download an official B-BBEE Audit Pack,
So that our verification agency can validate our ESD spend with immutable ledger evidence.
```

#### Technical Specification
1. Update [`frontend/src/routes/dashboard/corporate/+page.svelte`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/frontend/src/routes/dashboard/corporate/+page.svelte):
   - Replace static metric placeholders with live data from `api.corporate.getImpactReport()`.
   - Add "+ Commit New Cohort Funding" button opening a modal:
     - Cohort Name
     - Target Sector
     - Funding Amount (ZAR)
     - Youth Candidate Allocation
   - Implement "Download B-BBEE Audit Pack":
     - Generates a CSV or structured JSON report containing: Corporate Name, B-BBEE Verification Number, Transaction Timestamp, SME Beneficiary Name, Candidate Name, Verified Task Description, Rand Amount Disbursed, and Ledger Verification Hash.

#### Implementation Tasks
- [ ] Connect corporate dashboard to live API endpoints in `frontend/src/lib/api.js`.
- [ ] Build "Fund Cohort" modal with form validation and currency formatting.
- [ ] Build client-side or backend-driven B-BBEE CSV report generator.
- [ ] Add visual progress bars showing budget disbursement rates per cohort.

#### BDD Acceptance Criteria
```gherkin
Scenario: Downloading B-BBEE Audit Pack
  Given an authenticated corporate user with active cohort disbursements
  When they click "Download B-BBEE Audit Pack"
  Then a CSV file named "SSH_BBEE_Audit_Pack_<Date>.csv" downloads
  And it contains all verified tasks, SME beneficiaries, youth candidate details, and amounts
```

---

## Milestone 4: Admin Governance & Alumni Exit

### Issue #8: `feat(admin)`: Implement Admin User Management, Audit Logging, and Youth Alumni Placement API

- **Milestone:** Milestone 4 — Admin Governance & Alumni Exit
- **Labels:** `backend`, `security`, `governance`, `admin`
- **Priority:** P1 (High)

#### User Story
```gherkin
As a Platform Administrator,
I want to manage user accounts, review immutable audit trails, and mark youth candidates as placed alumni,
So that platform governance is maintained and candidate graduation outcomes are formally certified.
```

#### Technical Specification
1. **Audit Logging Integration:**
   - Create `AuditLogService.log(User user, String action, String entityType, Long entityId, String details)`.
   - Add automated triggers in `AuthService` (LOGIN, REGISTER), `TaskService` (CREATE, ASSIGN, COMPLETE), `ExperienceService` (VERIFY), and `PaymentService` (PAY).
2. **Admin Endpoints (`AdminController.java` at `/api/v1/admin`):**
   - `GET /api/v1/admin/users`: Paginated list of users with search and role filter (`YOUTH`, `SME`, `CORPORATE`).
   - `PATCH /api/v1/admin/users/{id}/toggle-status`: Enable or disable a user account.
   - `GET /api/v1/admin/audit-logs`: Searchable audit log feed with pagination and filters (`action`, `entityType`, `dateRange`).
   - `PATCH /api/v1/admin/youth/{youthId}/alumni`: Sets `isAlumni = true`, populates `alumniDate = now()`, and logs graduation audit event.

#### Implementation Tasks
- [ ] Create `AuditLogService` and inject into core domain services.
- [ ] Implement `AdminService` and `AdminController`.
- [ ] Protect all `/api/v1/admin/**` routes with `hasRole('ADMIN')`.
- [ ] Write integration test verifying audit logs are generated when tasks are created and verified.

#### BDD Acceptance Criteria
```gherkin
Scenario: Admin marks candidate as placed alumni
  Given a registered youth candidate active in the hub
  When an Admin calls "PATCH /api/v1/admin/youth/{id}/alumni"
  Then the youth's isAlumni attribute becomes true and alumniDate is set to the current timestamp
  And an AuditLog entry is recorded with action "GRADUATE_ALUMNI"
  And the youth's profile displays the permanent "PERMANENT ALUMNI" badge
```

---

### Issue #9: `feat(frontend)`: Build Admin Operations Dashboard with User Management & Audit Trail

- **Milestone:** Milestone 4 — Admin Governance & Alumni Exit
- **Labels:** `frontend`, `ui`, `admin`, `governance`
- **Priority:** P1 (High)

#### User Story
```gherkin
As a Platform Administrator,
I want an interactive operations interface to search users, inspect audit trails, and certify alumni exits,
So that I can effectively govern the platform without direct database queries.
```

#### Technical Specification
1. Update [`frontend/src/routes/dashboard/admin/+page.svelte`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/frontend/src/routes/dashboard/admin/+page.svelte):
   - Replace static cards with a 3-tab layout:
     - **Tab 1: Platform Users:** Searchable table with email, name, role badge, status toggle (`Active`/`Inactive`), and "Mark as Placed (Alumni)" action button.
     - **Tab 2: Audit Logs:** Real-time log table showing timestamp, actor email, action pill, target entity, and detail modal.
     - **Tab 3: System Overview:** Live counters for active tasks, verified records, disbursed capital, and alumni count.

#### Implementation Tasks
- [ ] Add `api.admin.getUsers()`, `api.admin.toggleUserStatus()`, `api.admin.getAuditLogs()`, and `api.admin.markAlumni()` to `api.js`.
- [ ] Build User Table with debounce search and role filter dropdown.
- [ ] Build Audit Trail log feed with JSON viewer for complex details.
- [ ] Add confirmation modal before marking a youth as alumni.

#### BDD Acceptance Criteria
```gherkin
Scenario: Admin toggles user account status
  Given a list of users on the admin dashboard
  When the Admin deactivates an SME account
  Then the user's status badge flips to "Suspended"
  And that SME is immediately blocked from logging in or posting tasks
```

---

## Milestone 5: Profile Management & Public Verifiable Ledger

### Issue #10: `feat(profile)`: Implement Youth & SME Profile View/Edit APIs and UI

- **Milestone:** Milestone 5 — Profile Management & Public Verifiable Ledger
- **Labels:** `backend`, `frontend`, `profile`
- **Priority:** P2 (Medium)

#### User Story
```gherkin
As a Youth Candidate,
I want to update my skills, education, bio, and portfolio link on my profile,
So that SMEs can review up-to-date capabilities when I apply for tasks.

As an SME Partner,
I want to manage our business description, CIPC registration, and B-BBEE level,
So that our profile is credible and verified to candidate applicants.
```

#### Technical Specification
1. **Backend Endpoints:**
   - `GET /api/v1/youth/me` & `PUT /api/v1/youth/me` (using [`YouthProfileRequest`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/dto/request/YouthProfileRequest.java)).
   - `GET /api/v1/sme/me` & `PUT /api/v1/sme/me` (using [`SMEProfileRequest`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/dto/request/SMEProfileRequest.java)).
2. **Frontend UI:**
   - Add Profile Edit route / modal accessible from [`Navbar.svelte`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/frontend/src/lib/components/Navbar.svelte).
   - Youth Profile: Skill tags input, portfolio URL, bio textarea, availability picker.
   - SME Profile: Business name, registration number, industry selector, B-BBEE level dropdown.

#### Implementation Tasks
- [ ] Implement `YouthController` (`/api/v1/youth`).
- [ ] Implement `SMEController` (`/api/v1/sme`).
- [ ] Add frontend Profile Settings modal/page.
- [ ] Write controller tests validating authorization rules.

#### BDD Acceptance Criteria
```gherkin
Scenario: Youth updates skill set
  Given an authenticated youth candidate
  When they update their profile with skills "Python, Data Scraping, Power BI"
  Then the database saves the updated skills list
  And subsequent task matching queries evaluate the newly added skills
```

---

### Issue #11: `feat(experience)`: Public Shareable Verification View for Youth Experience Ledger

- **Milestone:** Milestone 5 — Profile Management & Public Verifiable Ledger
- **Labels:** `frontend`, `backend`, `feature`, `experience-ledger`
- **Priority:** P2 (Medium)

#### User Story
```gherkin
As an SSH Youth Candidate,
I want a public, tamper-evident link to my Experience Ledger,
So that I can present verified, SME-rated proof of work on my CV and LinkedIn to permanent employers.
```

#### Technical Specification
1. **Backend:**
   - Add `publicLedgerUuid: UUID` to `Youth` entity (generated on creation).
   - Add unauthenticated public endpoint `GET /api/v1/public/ledger/{uuid}` returning youth name, location, alumni status, total verified tasks, and list of verified experiences (task title, SME business name, verified date, star rating, feedback).
2. **Frontend:**
   - Create public route `frontend/src/routes/verify/ledger/[uuid]/+page.svelte`.
   - Editorial, clean layout designed for printing or PDF export with official SSH verification seals and timestamps.
   - Add "Copy Public Ledger Link" button on `dashboard/youth/+page.svelte`.

#### Implementation Tasks
- [ ] Add `publicLedgerUuid` column to `Youth` entity.
- [ ] Add public controller endpoint in `PublicController.java`.
- [ ] Build public verification page in SvelteKit with OpenGraph meta tags for LinkedIn sharing.
- [ ] Add print stylesheet (`@media print`) for clean CV attachment generation.

#### BDD Acceptance Criteria
```gherkin
Scenario: External employer verifies youth work record
  Given a candidate with 3 verified tasks and public UUID "abc-123"
  When an employer visits "/verify/ledger/abc-123" without logging in
  Then they see the candidate's verified ledger with all 3 SME ratings and comments
  And unverified or private draft tasks are excluded
```

---

## Milestone 6: The SSH Guarantee Automation & Performance

### Issue #12: `feat(matching)`: Automated Youth-to-Task Skill Matching Engine & Fallback Internal Task Generator

- **Milestone:** Milestone 6 — The SSH Guarantee Automation & Performance
- **Labels:** `backend`, `automation`, `ssh-guarantee`, `scheduling`
- **Priority:** P2 (Medium)

#### User Story
```gherkin
As a Youth Candidate,
I want open tasks to be ranked according to my specific skill set,
So that I can quickly find and deliver high-value work suited to my training.

As the SSH Platform,
I want an automated job to generate paid internal tasks if SME task postings drop,
So that we uphold the SSH Guarantee: "You will not leave this platform without work and an income."
```

#### Technical Specification
1. **Skill Matching Engine:**
   - Create `MatchingService`: Calculates Jaccard similarity or tag-overlap score between `Youth.skills` and `Task.category`/`Task.description`.
   - Endpoint `GET /api/v1/tasks/recommended` returning tasks sorted by match percentage.
2. **Fallback Task Generator (`@Scheduled`):**
   - Create `InternalTaskGenerationJob` running daily at 06:00:
     - Checks total open tasks. If open tasks < threshold (e.g., 5), creates standardized internal tasks (e.g. "Platform Data Quality Audit", "Digital Literacy Feedback", "Community Hub Localization").
     - Assigns tasks to system entity "SSH Operations Internal".
     - Corporate reserve automatically funds standard stipends.

#### Implementation Tasks
- [ ] Build `MatchingService` with keyword extraction and scoring.
- [ ] Add `GET /api/v1/tasks/recommended` endpoint.
- [ ] Enable scheduling in `SshApplication`.
- [ ] Implement `InternalTaskGenerationJob` with configurable task thresholds in `application.properties`.

#### BDD Acceptance Criteria
```gherkin
Scenario: Fallback task generation triggers when task pool is depleted
  Given there are only 2 open SME tasks on the platform and threshold is 5
  When the internal task generation job runs
  Then 3 curated internal tasks are created with guaranteed corporate stipends
  And candidates can immediately apply for them
```

---

### Issue #13: `perf(redis)`: Implement Redis Caching for Task Directory and Session Invalidation

- **Milestone:** Milestone 6 — The SSH Guarantee Automation & Performance
- **Labels:** `backend`, `performance`, `redis`, `caching`
- **Priority:** P2 (Medium)

#### User Story
```gherkin
As a Platform User browsing hundreds of open tasks,
I want the task directory to load with sub-50ms latency,
So that navigating opportunities is instantaneous even under high concurrent traffic.
```

#### Technical Specification
1. In [`TaskService.java`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/service/TaskService.java):
   - Annotate `listOpenTasks()` with `@Cacheable(value = "tasks_open")`.
   - Annotate `getTaskById()` with `@Cacheable(value = "task_detail", key = "#id")`.
   - Annotate `createTask()`, `assignTask()`, and `completeTask()` with `@CacheEvict(value = {"tasks_open", "task_detail"}, allEntries = true)`.
2. Configure JSON serialization in [`RedisConfig.java`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/java/com/ssh/config/RedisConfig.java) with a 30-minute TTL.

#### Implementation Tasks
- [ ] Add caching annotations to `TaskService`.
- [ ] Ensure `TaskResponse` implements `Serializable` or configure Jackson 2 JSON serializer.
- [ ] Write integration test verifying cache hit on second call and cache eviction on task status change.

#### BDD Acceptance Criteria
```gherkin
Scenario: Task directory is served from cache
  Given open tasks are queried once and cached in Redis
  When a second request arrives for open tasks
  Then the database query is bypassed and the cached payload is returned in <50ms
  And when a new task is posted, the cache is evicted immediately
```

---

## Milestone 7: DevOps, Tooling & Testing Infrastructure

### Issue #14: `chore(db)`: Implement Liquibase Changelog Migrations & Disable Hibernate DDL Auto

- **Milestone:** Milestone 7 — DevOps, Tooling & Testing Infrastructure
- **Labels:** `backend`, `database`, `liquibase`, `devops`
- **Priority:** P1 (High)

#### User Story
```gherkin
As a DevOps Engineer and Backend Developer,
I want database schema changes to be tracked via versioned Liquibase changelogs,
So that migrations are repeatable, auditable, and safe across development, staging, and production environments.
```

#### Technical Specification
1. Create `backend/src/main/resources/db/changelog/db.changelog-master.yaml`.
2. Author YAML changesets for all tables:
   - `changelog-01-users.yaml`
   - `changelog-02-smes.yaml`
   - `changelog-03-youth.yaml`
   - `changelog-04-tasks.yaml`
   - `changelog-05-experiences.yaml`
   - `changelog-06-payments.yaml`
   - `changelog-07-audit-logs.yaml`
   - `changelog-08-task-applications.yaml`
   - `changelog-09-corporates-cohorts.yaml`
3. Update [`application.properties`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/src/main/resources/application.properties#L21-L34):
   - Set `spring.liquibase.enabled=true`.
   - Set `spring.jpa.hibernate.ddl-auto=validate`.

#### Implementation Tasks
- [ ] Create `db/changelog` directory and master changelog.
- [ ] Write all entity DDL definitions with foreign keys and unique constraints.
- [ ] Test fresh container spin-up with `docker compose up -d postgres` and Maven compile.

#### BDD Acceptance Criteria
```gherkin
Scenario: Clean database migration
  Given an empty PostgreSQL database instance
  When the Spring Boot application boots
  Then Liquibase executes all changelogs in sequence
  And tables users, smes, youth, tasks, experiences, payments, and audit_logs are created
  And DATABASECHANGELOG table records all executed changesets
```

---

### Issue #15: `chore(tooling)`: Add Maven Wrapper (`mvnw`), Fix JDK Compatibility, and Create `CONTRIBUTING.md`

- **Milestone:** Milestone 7 — DevOps, Tooling & Testing Infrastructure
- **Labels:** `devops`, `dx`, `documentation`
- **Priority:** P1 (High)

#### User Story
```gherkin
As an open-source contributor or team member,
I want standard build wrapper scripts and contributing guidelines in the repository,
So that I can clone and build the project immediately without build failures or version discrepancies.
```

#### Problem Analysis
- README instructs users to run `./mvnw spring-boot:run` and `./mvnw test`, but `mvnw` and `mvnw.cmd` do not exist.
- README references [`CONTRIBUTING.md`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/README.md#L212), but the file is missing.
- Compiler throws `ExceptionInInitializerError: EndPosTable` due to Lombok version mismatch on modern JDKs.

#### Technical Specification
1. Generate Maven wrapper: Run `mvn wrapper:wrapper` in `backend/`.
2. Update [`pom.xml`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/backend/pom.xml): Update Lombok version or configure Maven Compiler Plugin arguments (`-parameters`, JDK 17/21 compiler configuration).
3. Create root `CONTRIBUTING.md` documenting:
   - Branch naming (`feature/`, `bugfix/`, `hotfix/`).
   - Conventional commit format (`feat:`, `fix:`, `chore:`).
   - PR checklist and local testing commands.

#### Implementation Tasks
- [ ] Generate `mvnw`, `mvnw.cmd`, and `.mvn/wrapper/maven-wrapper.properties`.
- [ ] Fix compiler plugin configuration in `pom.xml`.
- [ ] Author `CONTRIBUTING.md`.

#### BDD Acceptance Criteria
```gherkin
Scenario: Developer builds backend using wrapper
  When running "./mvnw clean compile" from the backend directory
  Then the build completes with BUILD SUCCESS without requiring local Maven installation
```

---

### Issue #16: `test(backend)`: Comprehensive Unit and MockMvc Integration Test Suite

- **Milestone:** Milestone 7 — DevOps, Tooling & Testing Infrastructure
- **Labels:** `backend`, `testing`, `qa`
- **Priority:** P1 (High)

#### User Story
```gherkin
As a Lead Software Engineer,
I want high-coverage unit tests for services and MockMvc tests for controllers,
So that regressions are detected before pull requests merge into main.
```

#### Technical Specification
1. Add service unit tests with Mockito:
   - `AuthServiceTest`: valid register, duplicate email rejection, login success, invalid credentials.
   - `TaskServiceTest`: task creation, open task filtering, assignment authorization, completion logic.
   - `ExperienceServiceTest`: verification calculations, rating constraints.
   - `PaymentServiceTest`: 90/10 split validation, disbursement status update.
2. Add `@WebMvcTest` controller integration tests:
   - `AuthControllerTest`: HTTP 201 on register, HTTP 200 on login, HTTP 400 on blank inputs.
   - `TaskControllerTest`: HTTP 201 on task post, HTTP 403 when non-SME attempts to post.
   - `ExperienceControllerTest`: verification role checks and 404 handling.

#### Implementation Tasks
- [ ] Author all unit test classes under `backend/src/test/java/com/ssh/service/`.
- [ ] Author all controller test classes under `backend/src/test/java/com/ssh/controller/`.
- [ ] Achieve >80% line coverage on core business logic.

#### BDD Acceptance Criteria
```gherkin
Scenario: Running test suite
  When executing "mvn test"
  Then all test suites pass with zero failures
  And all REST endpoints have verified security and status code assertions
```

---

### Issue #17: `test(frontend)`: Setup Vitest, Playwright E2E Suite, and TypeScript Migration

- **Milestone:** Milestone 7 — DevOps, Tooling & Testing Infrastructure
- **Labels:** `frontend`, `testing`, `typescript`, `qa`
- **Priority:** P2 (Medium)

#### User Story
```gherkin
As a Frontend Engineer,
I want TypeScript types, component unit tests (Vitest), and end-to-end regression tests (Playwright),
So that UI refactors are type-safe and core user flows do not break.
```

#### Technical Specification
1. Add to [`frontend/package.json`](file:///C:/Users/tlhon/Documents/projects/Shared-Services-Hub/frontend/package.json):
   - Dev dependencies: `vitest`, `@testing-library/svelte`, `jsdom`, `@playwright/test`, `typescript`, `svelte-check`.
   - Scripts:
     ```json
     "test:unit": "vitest run",
     "test:e2e": "playwright test",
     "test": "vitest run && playwright test",
     "check": "svelte-check --tsconfig ./tsconfig.json"
     ```
2. Create `frontend/tsconfig.json` and configure SvelteKit TypeScript support.
3. Author E2E test `e2e/user-journey.spec.js`:
   - Register new SME -> Post task.
   - Register new Youth -> Apply for task.
   - SME assigns task.
   - Youth marks complete.
   - SME verifies task -> Checks experience ledger entry.

#### Implementation Tasks
- [ ] Install testing dependencies in `frontend/`.
- [ ] Configure `vite.config.js` with Vitest test environment.
- [ ] Add Playwright configuration and E2E test script.
- [ ] Add component unit tests for `StatusBadge.svelte` and `TaskCard.svelte`.

#### BDD Acceptance Criteria
```gherkin
Scenario: Automated frontend testing
  When executing "pnpm run test"
  Then all Vitest unit tests pass
  And Playwright runs headless browser testing the complete user journey with 100% success
```
