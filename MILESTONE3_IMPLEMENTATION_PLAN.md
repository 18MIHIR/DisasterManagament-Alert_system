# Milestone 3: Week 5 – Implementation Plan

## Current Status Assessment

### Database Schema vs Required Diagram

| Table | Status | Notes |
|-------|--------|-------|
| **users** | ✅ Match | id, name, email, password, role, phone, region |
| **disasters** | ✅ Match | Has id, type, location, severity, eventTime, status (plus extra useful columns) |
| **alerts** | ❌ Missing | No separate table; alert data embedded in Disaster |
| **rescue_tasks** | ❌ Missing | Not implemented |
| **reports** | ❌ Missing | Reports modeled as disasters; no dedicated table |

---

## 1. Alert Broadcasting & Management

### What Exists
- Admin can verify disasters and "push" (sets status ACTIVE, alertBroadcastAt, verifiedBy)
- Endpoint: `POST /api/disasters/{id}/verify`
- Dashboard shows active disasters to all logged-in users

### What’s Missing
- **Separate `alerts` table** per schema (id, disaster_id, message, created_by, broadcast_time, region)
- **Zone-based targeting**: Admin pushes to citizens in affected zones only
- **Instant notifications**: WebSocket/SSE or polling so citizens receive alerts in real time
- DisasterDTO does not expose `alertBroadcastAt` or `verifiedBy` to frontend

### Implementation Tasks
1. Create `Alert` entity and `alerts` table (disaster_id, message, created_by, broadcast_time, region)
2. Update `verifyAndPushAlert` to:
   - Create an `Alert` record with region = disaster’s affected region
   - Target citizens whose `users.region` matches affected region
3. Add `GET /api/alerts` and `GET /api/alerts/for-citizen` (returns alerts for the authenticated citizen’s region)
4. Add WebSocket or SSE to broadcast new alerts to connected citizens
5. Expose `alertBroadcastAt` and `verifiedBy` in DisasterDTO for UI

---

## 2. Acknowledge System

### What Exists
- Nothing

### What’s Missing
- `RescueTask` entity (rescue_tasks table)
- Responder acknowledgment flow
- Log acknowledgment timestamps

### Implementation Tasks
1. Create `RescueTask` entity:
   - id, responder_id (FK users), disaster_id (FK disasters), task_status, description, updated_at
   - TaskStatus enum: ASSIGNED, ACKNOWLEDGED, IN_PROGRESS, COMPLETED
2. Create `RescueTaskRepository`, `RescueTaskService`, `RescueTaskController`
3. Endpoints:
   - `POST /api/rescue-tasks` – Admin assigns task to responder
   - `GET /api/rescue-tasks/my-tasks` – Responder lists assigned tasks
   - `POST /api/rescue-tasks/{id}/acknowledge` – Responder confirms receipt (updates task_status, updated_at)
   - `GET /api/rescue-tasks/efficiency` – Admin views acknowledgment times
4. Frontend: Responder dashboard with task list and “Acknowledge” button

---

## 3. Citizen Reporting

### What Exists
- `ReportDisasterForm` – disaster report with location, description
- Only Admin and Responder can report; citizens cannot

### What’s Missing
- Citizens cannot submit emergency help requests
- No `reports` table (id, disaster_id, responder_id, details, submitted_at)
- No routing to nearest available responder

### Implementation Tasks
1. Create `Report` entity (reports table):
   - id, disaster_id (nullable FK), responder_id (nullable FK), details, submitted_at
   - Add `user_id` to identify citizen submitter; add `location` (or keep in details)
2. Create `ReportRepository`, `ReportService`, `ReportController`
3. Allow CITIZEN role to submit reports via `POST /api/reports`
4. Routing logic: find responders in same region, filter by availability (no ACKNOWLEDGED/IN_PROGRESS tasks), assign nearest (by region match, then by task load)
5. Frontend: Citizen “Request Help” form (separate from Report Disaster) – location, description; visible only to citizens

---

## 4. Tests

### What Exists
- Minimal `App.test.tsx` only
- No backend tests

### Implementation Tasks
1. Add Spring Boot test dependencies if not present
2. **Alert Broadcasting tests**:
   - Admin verifies disaster → alert created, region stored
   - Citizen in affected region receives alert via API
   - Citizen in other region does not receive alert (if zone-based)
3. **Responder acknowledgment tests**:
   - Responder can acknowledge task
   - task_status becomes ACKNOWLEDGED
   - updated_at records acknowledgment time
   - Admin can retrieve acknowledgment efficiency metrics
4. Create `AlertBroadcastingIntegrationTest` and `RescueTaskAcknowledgmentIntegrationTest`

---

## 5. Suggested Implementation Order

1. **Phase A – Schema**
   - Add `Alert`, `RescueTask`, `Report` entities
   - Run app to let Hibernate create/update tables

2. **Phase B – Acknowledge System**
   - RescueTask CRUD, acknowledge endpoint, frontend task list + acknowledge button
   - Quick to implement, clear schema

3. **Phase C – Alerts**
   - Alert entity + creation on verify
   - Zone-based filtering for citizens
   - DTO updates and frontend display of `alertBroadcastAt`

4. **Phase D – Citizen Reporting**
   - Report entity, citizen submit endpoint
   - Routing to nearest responder

5. **Phase E – Instant Notifications**
   - WebSocket/SSE for real-time alert delivery (optional enhancement)

6. **Phase F – Tests**
   - Alert broadcasting and responder acknowledgment integration tests

---

## File Changes Summary

### New Backend Files
- `entity/Alert.java`
- `entity/RescueTask.java`
- `entity/Report.java`
- `repository/AlertRepository.java`
- `repository/RescueTaskRepository.java`
- `repository/ReportRepository.java`
- `service/AlertService.java`
- `service/RescueTaskService.java`
- `service/ReportService.java`
- `controller/AlertController.java`
- `controller/RescueTaskController.java`
- `controller/ReportController.java`
- `dto/AlertDTO.java`, `RescueTaskDTO.java`, `ReportDTO.java`
- `AlertBroadcastingIntegrationTest.java`
- `RescueTaskAcknowledgmentIntegrationTest.java`

### Modified Backend Files
- `DisasterService.java` – create Alert on verify
- `DisasterDTO.java` – add alertBroadcastAt, verifiedBy
- `DisasterController.java` – allow CITIZEN to create reports (or use ReportController)
- `UserRepository.java` – add `findByRegionAndRole` for zone-based alerts

### New Frontend Files
- `ReportEmergencyForm.tsx` – citizen help request (location, description)
- `ResponderTasks.tsx` – responder task list + acknowledge button

### Modified Frontend Files
- `App.tsx` / `Dashboard.tsx` – show Report Emergency for citizens, ResponderTasks for responders
- `api.ts` – add alert, rescue-task, report endpoints

---

## Schema Verification Checklist

After implementation, verify:

- [ ] `alerts` table: id, disaster_id, message, created_by, broadcast_time, region
- [ ] `rescue_tasks` table: id, responder_id, disaster_id, task_status, description, updated_at
- [ ] `reports` table: id, disaster_id, responder_id, details, submitted_at (plus submitter/location if needed)
- [ ] All FKs and relationships correct
