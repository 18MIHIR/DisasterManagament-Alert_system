## Disaster Management & Alert System

**Tech stack**
- **Backend**: Java, Spring Boot, Spring Security, JWT, Spring Data JPA, MySQL
- **Frontend**: React (TypeScript, Create React App)
- **External APIs**: USGS Earthquake API, NASA EONET, Open-Meteo Weather API

### 1. Backend – Running locally



```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080`.

#### Main auth endpoints

- **POST** `http://localhost:8080/api/auth/register`
  - Body (JSON):
    - `name` (string)
    - `email` (string)
    - `password` (string, min 6 chars)
    - `phone` (string)
    - `region` (string)
    - `role` (string enum: `ADMIN`, `RESPONDER`, `CITIZEN`)
- **POST** `http://localhost:8080/api/auth/login`
  - Body (JSON):
    - `email`
    - `password`

Both return:

```json
{
  "token": "JWT_TOKEN_HERE",
  "role": "ADMIN | RESPONDER | CITIZEN",
  "name": "User Name",
  "region": "User Region"
}
```

The token is a **JWT** that will be used later to protect other APIs (disasters, alerts, tasks, etc.).

### 2. Frontend – Running locally

1. Open the `frontend` folder in a terminal.
2. Install dependencies:

```bash
npm install
```

3. Start the React dev server:

```bash
npm start
```

The app runs on `http://localhost:3000`.

The landing screen is the **Authentication** view with:
- Login (email + password)
- Toggle link to **Sign up**
- Registration fields: name, phone, location/region, and role buttons (Admin / Responder / Citizen)

On successful login/registration:
- The JWT token and basic user info are stored in `localStorage`:
  - `dm_token`
  - `dm_role`
  - `dm_name`
  - `dm_region`

---

## Milestone 2: Disaster Monitoring

### New Features

#### External API Integration
The system automatically fetches real-time disaster data from:
- **USGS Earthquake API** - Fetches earthquakes M4.5+ every 5 minutes
- **NASA EONET** - Earth Observatory Natural Event Tracker for wildfires, volcanoes, storms
- **Open-Meteo Weather API** - Weather alerts for major cities (extreme heat/cold, storms, floods)

#### Real-time Alert Dashboard
- Dynamic dashboard displaying ongoing and past disasters
- Stats panel showing active alerts by category
- Filtering by disaster type, severity, status, and region
- Auto-refresh every 60 seconds

#### Disaster Categorization
- Automatic tagging: FLOOD, CYCLONE, EARTHQUAKE, TSUNAMI, WILDFIRE, LANDSLIDE, DROUGHT, TORNADO, VOLCANIC_ERUPTION, SEVERE_STORM, HEATWAVE, COLDWAVE
- Severity levels: LOW, MODERATE, HIGH, CRITICAL, EXTREME
- Status tracking: PENDING, ACTIVE, RESOLVED, CANCELLED

#### Admin Verification Panel
- Admins can verify pending alerts before broadcast
- Edit disaster details (type, severity, description, etc.)
- Mark disasters as resolved or cancel false alarms

### New API Endpoints

#### Public Endpoints (no auth required)
- **GET** `/api/disasters` - Get all disasters with optional filters
  - Query params: `type`, `severity`, `status`, `region`
- **GET** `/api/disasters/{id}` - Get disaster by ID
- **GET** `/api/disasters/active` - Get active disasters
- **GET** `/api/disasters/pending` - Get pending disasters
- **GET** `/api/disasters/recent?hours=24` - Get recent disasters
- **GET** `/api/disasters/stats` - Get dashboard statistics
- **GET** `/api/disasters/types` - Get all disaster types
- **GET** `/api/disasters/severities` - Get all severity levels
- **GET** `/api/disasters/statuses` - Get all status options

#### Protected Endpoints (auth required)
- **POST** `/api/disasters` - Create a new disaster
- **PUT** `/api/disasters/{id}` - Update disaster details
- **POST** `/api/disasters/{id}/verify` - Verify and activate a disaster (Admin)
- **POST** `/api/disasters/{id}/resolve` - Mark disaster as resolved
- **POST** `/api/disasters/{id}/cancel` - Cancel a disaster alert
- **DELETE** `/api/disasters/{id}` - Delete a disaster
- **POST** `/api/disasters/fetch-external` - Manually trigger external API fetch

### Database Schema - Disasters Table

| Column | Type | Description |
|--------|------|-------------|
| id | Long | Primary key |
| title | String | Disaster title |
| description | Text | Detailed description |
| type | Enum | FLOOD, EARTHQUAKE, etc. |
| severity | Enum | LOW to EXTREME |
| status | Enum | PENDING, ACTIVE, RESOLVED, CANCELLED |
| location | String | Location description |
| region | String | Geographic region |
| country | String | Country name |
| latitude | Double | GPS latitude |
| longitude | Double | GPS longitude |
| source | String | Data source (USGS, NASA_EONET, etc.) |
| external_id | String | External API reference ID |
| event_time | DateTime | When the disaster occurred |
| created_at | DateTime | Record creation time |
| updated_at | DateTime | Last update time |
| resolved_at | DateTime | When marked resolved |
| verified_by | Long | Admin user ID who verified |
| advisory_message | String | Public advisory |
| affected_areas | String | List of affected areas |
| estimated_affected_population | Integer | Population impact |

---

### 3. Analytics & Alert Systems (Completed)

## Milestone 3: Alert Validation & Dispatch Systems

### New Features

#### Alert Broadcasting
- Separate `Alert` entity linking disasters to geographical regions.
- Zone-based dynamic broadcasting based on user location and disaster's location.
- Automatic dispatching to citizens upon verification.

#### Rescue Task Assignments (Responders)
- Admin can assign responders to specific disaster zones.
- Responders have dedicated dashboards with a Task List.
- Acknowledge system with state tracking (`ASSIGNED`, `ACKNOWLEDGED`, `IN_PROGRESS`, `COMPLETED`).

#### Citizen Emergency Reporting
- Dedicated "Request Help" forms for Citizens.
- Spatial-routing of incident requests to the nearest available responders.
- Real-time DB tracking and status tracking.

## Milestone 4: Interactive Analytics & KPIs

### Enhanced Data Visualization
- Integration of `recharts` for dynamic data visualizations.
- Dashboard for viewing Disasters Trends, Category Breakdowns, and Alerts by Region.
- Performance KPI tracking including responder acknowledgement times and engagement rates.
- Fully responsive, premium UI.
