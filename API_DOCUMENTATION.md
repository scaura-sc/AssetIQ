# AssetIQ Backend — API Reference

For UI integration against the local backend.

- **Base URL (same machine):** `http://localhost:8081`
- **Base URL (LAN):** `http://192.168.0.100:8081` *(DHCP-assigned — re-check with `ipconfig getifaddr en0` if it stops working; port is 8081, not 8080 — 8080 is used by another local project on this machine)*
- **Demo tenant:** `coca-cola`
first
---

## 1. Conventions every screen needs to know

### 1.1 Tenant header (required on every `/api/**` call)

Every request must include:

```
X-Tenant-Id: coca-cola
```

Missing/blank header → `400 Bad Request` with body `{"message":"Missing required header: X-Tenant-Id"}` **before** the request even reaches a controller. There is no path- or query-based tenant option — header only.

### 1.2 `Long` IDs are serialized as JSON **strings** — treat every ID as an opaque string

`id`, `assetId`, `oldAssetId`, `newAssetId`, etc. are Snowflake-generated 64-bit values that routinely exceed JavaScript's `Number.MAX_SAFE_INTEGER` (2^53−1). To avoid silent precision loss, **every `Long` field is serialized as a JSON string in responses**, e.g.:

```json
{ "id": "205628492535037952", "assetId": "900003001" }
```

**UI rule of thumb:**
- Never run an ID through `Number(...)`/`parseInt(...)` — keep it as a string end-to-end (state, routing, request bodies).
- When *sending* an ID back in a request body (e.g. `SwapRequest.oldAssetId`), a quoted JSON string works — Jackson deserializes a JSON string into a `Long` field with no extra config.
- Path variables (e.g. `/api/assets/{id}`) are plain strings in the URL — no special handling needed there.

### 1.3 Error shape (all 4xx from any endpoint)

```json
{
  "timestamp": "2026-07-23T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Asset 999 not found",
  "path": "/api/assets/999"
}
```

- `404` — entity not found
- `400` — bad request / validation failure (message lists each failing field as `field: reason`, joined with `; `)
- `409` — conflict (e.g. duplicate code, invalid state transition)

### 1.4 Dates & times

- `LocalDate` fields (e.g. `purchaseDate`) → `"2026-07-23"`
- `LocalDateTime` fields (e.g. `capturedAt`) → `"2026-07-23T12:00:00"` (no timezone — server-local)

### 1.5 CORS

No CORS configuration exists yet. If the UI is a browser app served from a different origin (e.g. a dev server on `localhost:3000`/`5173`), calls will be blocked by the browser until CORS is added — flag this if it comes up.

### 1.6 Pagination

No endpoint paginates. List endpoints return the full result set for the tenant (and any filters given) in one array.

---

## 2. Enum reference

| Enum | Values |
|---|---|
| `AssetStatus` | `STOCK`, `DEPLOYED`, `IN_TRANSIT`, `UNDER_REPAIR`, `MISSING`, `STOLEN`, `RETIRED`, `SCRAPPED` |
| `WorkingStatus` | `WORKING`, `NOT_WORKING`, `PARTIAL` |
| `ConditionGrade` | `EXCELLENT`, `GOOD`, `FAIR`, `POOR`, `SCRAP` |
| `LocationType` | `STOCK`, `OUTLET`, `DISTRIBUTOR`, `WAREHOUSE`, `VEHICLE`, `EMPLOYEE` |
| `WarrantyType` | `OEM`, `EXTENDED`, `AMC`, `NONE` |
| `DepreciationMethod` | `SLM`, `WDV` |
| `AhsConfidenceLevel` | `HIGH`, `MEDIUM`, `LOW`, `STALE` |
| `CatalogLevel` | `CATEGORY`, `TYPE`, `MODEL` |
| `VendorType` | `SUPPLIER`, `SERVICE`, `BOTH` |
| `AssignmentType` | `PERMANENT`, `TEMPORARY`, `LOAN` |
| `DepositStatus` | `PAID`, `PENDING`, `WAIVED` |
| `MovementType` | `ASSIGN`, `TRANSFER`, `RETURN`, `SWAP_OUT`, `SWAP_IN`, `SCRAP`, `LOST`, `FOUND`, `TEMP_ASSIGN`, `TEMP_RETURN` |
| `RoleCode` | `SALESMAN`, `SUPERVISOR`, `ASM`, `TECHNICIAN` |
| `PresenceStatus` | `PRESENT`, `NOT_FOUND`, `PARTIAL` |
| `ScanMethod` | `QR`, `BARCODE`, `RFID`, `NFC`, `MANUAL`, `NOT_SCANNED` |
| `DetectionSource` | `MANUAL`, `AI_VISION` |
| `BrandingStatus` | `INTACT`, `PARTIAL_DAMAGE`, `NO_BRANDING` |
| `ComplaintType` | `NOT_WORKING`, `COOLING_ISSUE`, `GAS_REFILL`, `LEAKAGE`, `PHYSICAL_DAMAGE`, `BRANDING_DAMAGE`, `NOISE`, `CLEANING`, `COMPRESSOR`, `IOT_ALERT`, `WARRANTY_CLAIM`, `OTHER` |
| `WorkOrderType` | `PREVENTIVE`, `CORRECTIVE`, `EMERGENCY` |
| `TriggeredBy` | `SCHEDULE`, `COMPLAINT`, `AI_PREDICTION`, `MANUAL` |
| `Priority` | `CRITICAL`, `HIGH`, `MEDIUM`, `LOW` |
| `EventType` | `COMPLAINT`, `WORK_ORDER` |
| `EventStatus` | `OPEN`, `ASSIGNED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `REOPENED` |

---

## 3. Asset Catalog — `/api/asset-catalog`

Hierarchy: `CATEGORY` → `TYPE` (parentCode = a CATEGORY code) → `MODEL` (parentCode = a TYPE code).

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/asset-catalog` | Create a catalog entry (any level) |
| `PUT` | `/api/asset-catalog/{id}` | Update name/description/defaults (level & parentCode are immutable) |
| `GET` | `/api/asset-catalog/{id}` | Get one entry |
| `GET` | `/api/asset-catalog?level=CATEGORY` | List all entries at a level |
| `GET` | `/api/asset-catalog/children?parentCode=COOLER` | List direct children of a code |
| `GET` | `/api/asset-catalog/models/available` | List all active MODEL-level entries |
| `DELETE` | `/api/asset-catalog/{id}` | Deactivate (soft delete) → `204` |

**`POST` / request body (`CatalogCreateRequest`):**
```json
{
  "level": "MODEL",
  "code": "VC-300L",
  "name": "VisiCooler 300L",
  "parentCode": "VISI_COOLER",
  "description": null,
  "manufacturerName": "Western Refrigeration Pvt Ltd",
  "manufacturerCountry": "India",
  "manufacturerContactEmail": null,
  "manufacturerContactPhone": null,
  "defaultWarrantyMonths": 24,
  "defaultUsefulLifeYears": 8,
  "defaultDepreciationMethod": "SLM",
  "defaultPmFrequencyDays": 90,
  "defaultPurityClausePct": 95.00,
  "capacity": 300.00,
  "capacityUnit": "Litres"
}
```
Required: `level`, `code`, `name`. `parentCode` required for `TYPE`/`MODEL`, must be `null` for `CATEGORY`.

**`PUT` body (`CatalogUpdateRequest`):** same as above minus `level`/`code`/`parentCode` (`name` required).

**Response (`CatalogResponse`):** create/update fields above **plus** `id`, `tenantId`, `isActive`, `createdAt`, `updatedAt`.

---

## 4. Vendors — `/api/vendors`

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/vendors` | Create vendor |
| `GET` | `/api/vendors/{id}` | Get one vendor |
| `GET` | `/api/vendors` | List active vendors |
| `DELETE` | `/api/vendors/{id}` | Deactivate → `204` |

**`POST` body (`VendorCreateRequest`):**
```json
{
  "vendorCode": "V-001",
  "vendorName": "Western Refrigeration Pvt Ltd",
  "vendorType": "SUPPLIER",
  "gstNumber": "27ABCDE1234F1Z5",
  "contactEmail": "sales@westernrefrigeration.example",
  "contactPhone": "+91-9000010001"
}
```
Required: `vendorCode`, `vendorName`, `vendorType`.

**Response (`VendorResponse`):** body fields above **plus** `id`, `tenantId`, `isActive`, `createdAt`, `updatedAt`.

---

## 5. Assets — `/api/assets`

### 5.1 Registration & lookup

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/assets` | Register a new asset (always created with `assetStatus: STOCK`) |
| `GET` | `/api/assets/{id}` | Get one asset |
| `GET` | `/api/assets` | List **all** assets for the tenant (no filters) |

**`POST` body (`AssetCreateRequest`):**
```json
{
  "serialNumber": "SN-CC-1003",
  "assetName": "VisiCooler 300L - Unit 3",
  "categoryCode": "COOLER",
  "typeCode": "VISI_COOLER",
  "modelCode": "VC-300L",
  "vendorCode": "V-001",
  "brandCode": null,
  "divisionCode": null,
  "companyCode": null,
  "capacity": 300.00,
  "capacityUnit": "Litres",
  "colour": "Red",
  "purchaseDate": "2025-05-20",
  "purchasePrice": 46000.00,
  "purchaseOrderRef": null,
  "invoiceRef": null,
  "manufacturingDate": null,
  "warrantyStartDate": "2025-05-20",
  "warrantyEndDate": "2027-05-20",
  "warrantyType": "OEM",
  "amcStartDate": null,
  "amcEndDate": null,
  "amcVendorCode": null,
  "depreciationMethod": "SLM",
  "usefulLifeYears": 8,
  "residualValue": null,
  "createdBy": "system"
}
```
Required: `serialNumber`, `assetName`, `categoryCode`, `typeCode`, `modelCode`, `purchaseDate`, `purchasePrice`, `createdBy`.
Notes: `assetNumber` is server-generated — **don't** send it. There is no field to set `assetStatus` on create — it's always `STOCK`.

**Response (`AssetResponse`)** — everything above plus deployment/AHS-scoring state: `assetStatus`, `workingStatus`, `conditionGrade`, `locationType`, `locationCode`, `territoryCode`, `salesmanCode`, `installationDate`, `lastVisitDate`, `lastVisitId`, `ahsScore`, `ahsPresenceScore`, `ahsPurityScore`, `ahsConditionScore`, `ahsUptimeScore`, `ahsPlFactor`, `ahsConfidenceLevel`, `ahsCalculatedAt`, `ahsStaleFlag`, `ahsStaleSince`, `primaryPhotoUrl`, `documentRefs`, `id`, `tenantId`, `createdAt`, `updatedBy`, `updatedAt`, `isActive`.

### 5.2 Deployment lifecycle

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/assets/{id}/deploy` | Deploy a `STOCK` asset to an **outlet** |
| `POST` | `/api/assets/{id}/transfer` | Move an asset to **any** location type / status |
| `POST` | `/api/assets/swap` | Swap a deployed asset out for a stock asset, at the same outlet, atomically |
| `GET` | `/api/assets/{id}/associations/current` | Current active association (`200` + body, or `204` if none) |
| `GET` | `/api/assets/{id}/associations` | Full association history |
| `GET` | `/api/assets/{id}/movements` | Full movement-log history |

**`POST /{id}/deploy` body (`DeployRequest`)** — destination is always `OUTLET`:
```json
{
  "outletCode": "OUT-JAYANAGAR",
  "outletName": "Jayanagar Outlet",
  "territoryCode": "TER-BLR-01",
  "salesmanCode": "SM-1001",
  "custodianName": null,
  "custodianPhone": null,
  "assignmentDate": "2025-05-22",
  "expectedReturnDate": null,
  "assignmentType": "PERMANENT",
  "assignmentRef": null,
  "hasContract": false,
  "contractRef": null,
  "depositAmount": null,
  "depositStatus": null,
  "exclusivityFlag": false,
  "purityClausePct": null,
  "contractStartDate": null,
  "contractEndDate": null,
  "contractDocumentUrl": null,
  "movedByUserCode": "SM-1001",
  "reason": "Initial deployment",
  "gpsLat": 12.9308,
  "gpsLng": 77.5838
}
```
Required: `outletCode`, `assignmentDate`, `movedByUserCode`.

**Preconditions — violating any of these returns `409 Conflict`:**
- The asset's `assetStatus` must be `STOCK` (deploying an already-deployed asset is rejected — use transfer/swap instead).
- The asset's `workingStatus` must be `WORKING`. A freshly-registered asset has `workingStatus: null` (it's never been captured yet) and **cannot** be deployed until a visit capture sets it to `WORKING` — error: `"Asset {assetNumber} must be WORKING to deploy (is {workingStatus})"`.
- The asset must **not** already have an active association — check `GET /api/assets/{id}/associations/current` returns `204` (not `200`) before deploying. Error if it does: `"Asset {id} already has an active association; deactivate it first"`.

Practical flow for the UI: `GET /api/assets`, filter to `assetStatus: "STOCK"` and `workingStatus: "WORKING"`, then confirm `GET /api/assets/{id}/associations/current` is `204` before offering "deploy" for that asset.

**Response (`DeployResponse`):** `{ "asset": AssetResponse, "association": AssociationResponse, "movementLog": MovementLogResponse }`

**`POST /{id}/transfer` body (`TransferRequest`)** — destination can be any `LocationType`, and `targetAssetStatus` is explicit (allowed: `DEPLOYED`, `UNDER_REPAIR`, `STOCK`, `RETIRED`, `SCRAPPED` — the last two are irreversible):
```json
{
  "toLocationType": "WAREHOUSE",
  "toLocationCode": "WH-BLR-CENTRAL",
  "toLocationName": null,
  "territoryCode": null,
  "salesmanCode": null,
  "custodianName": null,
  "custodianPhone": null,
  "targetAssetStatus": "STOCK",
  "assignmentDate": "2026-07-23",
  "expectedReturnDate": null,
  "assignmentType": null,
  "assignmentRef": null,
  "hasContract": false,
  "contractRef": null,
  "depositAmount": null,
  "depositStatus": null,
  "exclusivityFlag": false,
  "purityClausePct": null,
  "contractStartDate": null,
  "contractEndDate": null,
  "contractDocumentUrl": null,
  "movedByUserCode": "SM-1001",
  "approvedByUserCode": null,
  "approvalRef": null,
  "reason": "Outlet closure",
  "gpsLat": null,
  "gpsLng": null
}
```
Required: `toLocationType`, `toLocationCode`, `targetAssetStatus`, `assignmentDate`, `movedByUserCode`, `reason`.

**Response (`TransferResponse`):** `{ "asset": AssetResponse, "newAssociation": AssociationResponse, "movementLog": MovementLogResponse }`

**`POST /swap` body (`SwapRequest`)** — `oldAssetId` (currently deployed) is swapped out, `newAssetId` (currently `STOCK`) takes its place at the same outlet:
```json
{
  "oldAssetId": "900003001",
  "newAssetId": "900003003",
  "swapReference": null,
  "movedByUserCode": "SM-1001",
  "reason": "Cooling issue — unit replaced",
  "gpsLat": null,
  "gpsLng": null
}
```
Required: `oldAssetId`, `newAssetId`, `movedByUserCode`. `swapReference` is optional — server mints one if omitted (used to pair the two resulting movement-log rows).

**Response (`SwapResponse`):** `{ "oldAsset": AssetResponse, "newAsset": AssetResponse, "newAssociation": AssociationResponse, "swapOutMovement": MovementLogResponse, "swapInMovement": MovementLogResponse, "swapReference": string }`

**`AssociationResponse` fields:** `id`, `tenantId`, `assetId`, `assetNumber`, `locationType`, `locationCode`, `locationName`, `territoryCode`, `salesmanCode`, `custodianName`, `custodianPhone`, `assignmentDate`, `expectedReturnDate`, `assignmentType`, `assignmentRef`, `hasContract`, `contractRef`, `depositAmount`, `depositStatus`, `exclusivityFlag`, `purityClausePct`, `contractStartDate`, `contractEndDate`, `contractDocumentUrl`, `isActive`, `createdBy`, `createdAt`.

**`MovementLogResponse` fields:** `id`, `tenantId`, `assetId`, `assetNumber`, `movementType`, `fromLocationType`, `fromLocationCode`, `toLocationType`, `toLocationCode`, `movedByUserCode`, `approvedByUserCode`, `approvalRef`, `reason`, `gpsLat`, `gpsLng`, `movedAt`.

---

## 6. Visit Captures — `/api/visit-captures`

Field-rep asset checks during outlet visits, with purity/condition scoring.

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/visit-captures` | Record a visit capture |
| `GET` | `/api/visit-captures/{id}` | Get one capture |
| `GET` | `/api/visit-captures?assetId=900003001` | List all captures for an asset |

**`POST` body (`VisitCaptureCreateRequest`):**
```json
{
  "visitId": "VISIT-CC-0005",
  "visitDate": "2026-07-23",
  "outletCode": "OUT-JAYANAGAR",
  "territoryCode": "TER-BLR-01",
  "salesmanCode": "SM-1001",
  "assetId": "900003004",
  "roleCode": "SALESMAN",
  "isPlannedVisit": true,
  "presenceStatus": "PRESENT",
  "scanMethod": "QR",
  "scanValue": "QR-AST-CC-0004",
  "purityRawScore": null,
  "purityPct": 96.5,
  "purityAiConfidence": 91.0,
  "conditionGrade": "GOOD",
  "conditionAiConfidence": 88.5,
  "workingStatus": "WORKING",
  "competitorPresent": false,
  "competitorBrand": null,
  "competitorPct": null,
  "brandingStatus": "INTACT",
  "photoUrl1": null,
  "photoUrl2": null,
  "photoUrl3": null,
  "gpsLat": 12.9308,
  "gpsLng": 77.5838,
  "gpsAccuracyM": 5.0,
  "capturedOffline": false,
  "syncedAt": null,
  "capturedAt": "2026-07-23T11:15:00",
  "notes": null
}
```
Required: `visitId`, `visitDate`, `outletCode`, `salesmanCode`, `assetId`, `roleCode`, `presenceStatus`, `capturedAt`.

**Important — purity is one of two mutually exclusive input paths:**
- **Manual path:** send `purityRawScore` (integer 1–5) → server converts to `purityPct` internally.
- **AI Vision path:** send `purityPct` directly (optionally with `purityAiConfidence`).
- Sending **both** `purityRawScore` and `purityPct` is rejected (`400`).

**Response (`VisitCaptureResponse`):** request fields above plus `id`, `tenantId`, `assetNumber`, `puritySource` (`MANUAL`/`AI_VISION`, derived from which path was used), `conditionSource`, `workingStatusSource`, `createdAt`.

---

## 7. Service Events (Complaints & Work Orders) — `/api/service-events`

One table backs both complaints and work orders (`eventType` discriminates); `ServiceEventResponse` is the flat shape returned everywhere.

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/service-events/complaints` | Raise a complaint |
| `POST` | `/api/service-events/work-orders` | Create a work order directly |
| `POST` | `/api/service-events/work-orders/preventive-maintenance` | Schedule a PM work order (`woType`/`triggeredBy` are implicit) |
| `GET` | `/api/service-events/work-orders/overdue` | List overdue PM work orders |
| `PATCH` | `/api/service-events/{id}/status` | Transition status |
| `PATCH` | `/api/service-events/{id}/assign` | Assign/reassign a technician |
| `POST` | `/api/service-events/{id}/close` | Close a work order |
| `GET` | `/api/service-events/search` | Filtered search (dashboard/list view) |
| `GET` | `/api/service-events/mttr` | Avg resolution time per complaint type |
| `GET` | `/api/service-events/{id}` | Get one event |
| `GET` | `/api/service-events?assetId=900003001` | List events for an asset |

**`POST /complaints` body (`ComplaintCreateRequest`):**
```json
{
  "assetId": "900003004",
  "outletCode": "OUT-JAYANAGAR",
  "visitId": null,
  "priority": "HIGH",
  "description": "Cooler not maintaining temperature",
  "raisedByUserCode": "SM-1001",
  "complaintType": "COOLING_ISSUE",
  "photoUrl1": null,
  "photoUrl2": null,
  "gpsLat": null,
  "gpsLng": null,
  "raisedAt": "2026-07-23T10:05:00"
}
```
Required: `assetId`, `outletCode`, `priority`, `raisedByUserCode`, `complaintType`, `raisedAt`.
Note: `isUnderWarranty` and `isRepeated` are computed server-side — don't send them.

**`POST /work-orders` body (`WorkOrderCreateRequest`):**
```json
{
  "assetId": "900003004",
  "outletCode": "OUT-JAYANAGAR",
  "priority": "HIGH",
  "raisedByUserCode": "system",
  "assignedToUserCode": "tech-2001",
  "woType": "CORRECTIVE",
  "triggeredBy": "COMPLAINT",
  "plannedDate": "2026-07-25",
  "labourCost": 500.00,
  "partsCost": 300.00,
  "checklistSummary": null,
  "raisedAt": "2026-07-23T10:10:00"
}
```
Required: `assetId`, `outletCode`, `priority`, `raisedByUserCode`, `woType`, `triggeredBy`, `raisedAt`.
Note: `totalCost` is always server-recomputed from `labourCost + partsCost` — don't send it.

**`POST /work-orders/preventive-maintenance` body (`PreventiveMaintenanceRequest`):**
```json
{
  "assetId": "900003004",
  "outletCode": "OUT-JAYANAGAR",
  "priority": "LOW",
  "raisedByUserCode": "system",
  "assignedToUserCode": "tech-2001",
  "plannedDate": "2026-08-01",
  "raisedAt": "2026-07-23T09:00:00"
}
```
Required: `assetId`, `outletCode`, `priority`, `raisedByUserCode`, `plannedDate`, `raisedAt`. `woType`/`triggeredBy` are always `PREVENTIVE`/`SCHEDULE` — not settable.

**`PATCH /{id}/status` body:** `{ "status": "IN_PROGRESS" }` — any `EventStatus` value.

**`PATCH /{id}/assign` body:** `{ "assignedToUserCode": "tech-2001" }`

**`POST /{id}/close` body (`CloseWorkOrderRequest`):**
```json
{
  "photoAfterUrl": "https://cdn.example.com/wo/0001/after.jpg",
  "signatureUrl": "https://cdn.example.com/wo/0001/signature.png",
  "resolutionNotes": "Replaced compressor",
  "labourCost": 500.00,
  "partsCost": 300.00
}
```
Required: `photoAfterUrl`, `signatureUrl` (both mandatory to close). `labourCost`/`partsCost` optional — override the creation-time values if given; `totalCost` is always recomputed.

**`GET /search` query params (all optional, combine freely):**
`eventType`, `status`, `priority`, `outletCode`, `woType`, `territoryCode`, `assignedToUserCode`, `raisedAfter` (ISO datetime, e.g. `2026-07-01T00:00:00`), `raisedBefore`.
→ Returns `ServiceEventDashboardItem[]`: `{ "event": ServiceEventResponse, "slaDueAt": "...", "slaBreached": boolean }`. SLA windows from `raisedAt`: CRITICAL 4h, HIGH 24h, MEDIUM 72h, LOW 7 days.

**`GET /mttr`** → `MttrByComplaintType[]`: `{ "complaintType": "COOLING_ISSUE", "avgResolutionHours": 18.5, "sampleSize": 12 }`

**`ServiceEventResponse` full field list:** `id`, `tenantId`, `eventNumber`, `eventType`, `assetId`, `assetNumber`, `outletCode`, `visitId`, `priority`, `status`, `description`, `raisedByUserCode`, `assignedToUserCode`, `resolvedAt`, `closedAt`, `resolutionNotes`, `photoUrl1`, `photoUrl2`, `photoAfterUrl`, `signatureUrl`, `gpsLat`, `gpsLng`, `complaintType`, `isUnderWarranty`, `isRepeated`, `parentEventNumber`, `customerRating`, `woType`, `triggeredBy`, `plannedDate`, `startedAt`, `completedAt`, `labourCost`, `partsCost`, `totalCost`, `checklistSummary`, `raisedAt`, `createdAt`, `updatedAt`.

---

## 8. Asset Requests — `/api/asset-requests`

The flow for an outlet that needs an asset but doesn't know (or care) which specific one it'll get — it only knows the **category + type** it wants (e.g. "a COOLER of type VISI_COOLER"). A portal user then checks what's actually in stock matching that category/type and approves the request against one specific asset — which **deploys it** to the outlet in the same step (reuses `POST /api/assets/{id}/deploy`'s own logic and preconditions internally, so nothing here bypasses those rules).

**Flow:**
1. Outlet/salesman → `POST /api/asset-requests` (category + type only) → `status: PENDING`.
2. Portal → `GET /api/asset-requests?status=PENDING` to see what needs action.
3. Portal → `GET /api/asset-requests/{id}/available-stock` to see which real assets could fulfill it.
4. Portal → `POST /api/asset-requests/{id}/approve` with one of those asset ids → deploys it and marks the request `APPROVED`. Or `POST /api/asset-requests/{id}/reject` with a reason.

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/asset-requests` | Outlet raises a request (category + type only) |
| `GET` | `/api/asset-requests/{id}` | Get one request |
| `GET` | `/api/asset-requests?status=&outletCode=&territoryCode=` | Portal list/search (all filters optional, combine freely) |
| `GET` | `/api/asset-requests/{id}/available-stock` | Eligible assets matching the requested category/type — `STOCK`, `WORKING`, no active association |
| `POST` | `/api/asset-requests/{id}/approve` | Pick an asset from available-stock, approve, and deploy it |
| `POST` | `/api/asset-requests/{id}/reject` | Decline the request with a reason |

**`POST` body (`AssetRequestCreateRequest`):**
```json
{
  "outletCode": "OUT-JAYANAGAR",
  "outletName": "Jayanagar Outlet",
  "territoryCode": "TER-BLR-01",
  "salesmanCode": "SM-1001",
  "categoryCode": "FRIDGE",
  "typeCode": "DEEP_FREEZER",
  "reason": "Outlet needs a replacement freezer",
  "requestedByUserCode": "SM-1001",
  "requestedAt": "2026-07-28T11:00:00"
}
```
Required: `outletCode`, `salesmanCode`, `categoryCode`, `typeCode`, `requestedByUserCode`, `requestedAt`. No `modelCode` field at all — that's the point, the outlet doesn't specify a model. `categoryCode`/`typeCode` must form a valid chain (same catalog validation as asset registration) or this returns `400`.

**Response (`AssetRequestResponse`):** all of the above, plus `id`, `tenantId`, `requestNumber` (server-generated, e.g. `AR-1KTG678DF474`), `status` (`PENDING`/`APPROVED`/`REJECTED`), `approvedAssetId`, `approvedByUserCode`, `approvedAt`, `rejectionReason`, `rejectedByUserCode`, `rejectedAt`, `createdAt`, `updatedAt`.

**`GET /{id}/available-stock`** → `AssetResponse[]` (same shape as `GET /api/assets`) — every asset the tenant owns matching this request's `categoryCode`+`typeCode`, filtered to:
- `assetStatus: STOCK` + `workingStatus: WORKING`
- its current association (if any) is either absent or at a `WAREHOUSE` — a warehouse-associated asset *is* "in stock and available," not excluded. Anything actively associated to an `OUTLET`/`DISTRIBUTOR`/`VEHICLE`/`EMPLOYEE` is already spoken for and won't appear here.
- **if the request has a `territoryCode`, the asset's own `territoryCode` must match it** — stock sitting in a warehouse in a different territory isn't realistically available to this outlet, so it's excluded. (If the request has no `territoryCode` at all, this constraint is skipped rather than excluding everything.) Note this means an asset whose own `territoryCode` was never set (e.g. moved into a warehouse without specifying one) won't appear for *any* territory-scoped request — that's a data-completeness issue on the asset, not a bug in this filter.

There's no separate "stock quantity" field or counter anywhere in this system — assets are tracked as individually serialized units, not bulk SKU+quantity inventory. **The array length of this response *is* the live available quantity** for this request's category/type/territory; this is exactly what a portal screen should call to show the approver both the count and the specific units they can choose from before approving.

**On approve, which of `deploy`/`transfer` runs is decided automatically** based on the chosen asset's current state — not something the caller needs to think about: if it has no prior association, `deploy()` runs (fresh stock); if it has an active warehouse association, `transfer()` runs instead (deactivates the warehouse association, creates the new outlet one). Both paths end with the same result — the asset is `DEPLOYED` at the request's outlet — just via whichever underlying operation is valid for that asset's starting state.

**`POST /{id}/approve` body (`AssetRequestApproveRequest`):**
```json
{
  "assetId": "205668755760480256",
  "approvedByUserCode": "portal-admin-1",
  "reason": "Approved from stock"
}
```
Required: `assetId` (should be one from `available-stock`, though it's re-validated regardless — see below), `approvedByUserCode`. `reason` optional — falls back to the original request's `reason` if omitted.

Approving **re-runs every deploy precondition** via the same code path as `/deploy` — if the chosen `assetId` isn't actually `STOCK`+`WORKING` with no active association anymore (e.g. a race with another approval), you get the same `409` `deploy` would give, e.g. `"Asset AST-CC-0005 must be STOCK to deploy (is DEPLOYED)"`, and the request stays `PENDING` (nothing is partially applied). Approving a request that's already `APPROVED`/`REJECTED` also returns `409`: `"Asset request {requestNumber} is already {status}"`.

**`POST /{id}/reject` body (`AssetRequestRejectRequest`):**
```json
{
  "rejectedByUserCode": "portal-admin-1",
  "rejectionReason": "No stock available for this type currently"
}
```
Both fields required. Same "already APPROVED/REJECTED" `409` guard as approve.

---

## 9. Scoring Config & AHS (Asset Health Score) — `/api/scoring-config`

Every asset carries an **AHS** (`ahsScore` on `AssetResponse`, 0-100) computed from 4 weighted components — **Presence, Purity, Condition, Uptime** — every time a visit capture is submitted. `/api/scoring-config` lets each tenant configure how much each component counts.

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/scoring-config` | Get the tenant's current weights (or the 25/25/25/25 default if never configured) |
| `PUT` | `/api/scoring-config` | Set the tenant's weights |

**Body / response (`ScoringConfigRequest` / `ScoringConfigResponse`):**
```json
{
  "presenceWeightPct": 20.00,
  "purityWeightPct": 35.00,
  "conditionWeightPct": 30.00,
  "uptimeWeightPct": 15.00
}
```
The 4 weights **must sum to exactly 100** and each be ≥ 0, or `PUT` returns `400`. The `coca-cola` demo tenant is pre-seeded with `20/35/30/15` (not the default split), so you can see configured weights actually changing the math.

### How the score is computed (on every `POST /api/visit-captures`)

Each of the 4 components maps a signal from that specific capture onto a 0-100 scale:

| Component | Source field on the capture | Mapping |
|---|---|---|
| Presence | `presenceStatus` (always present — required field) | `PRESENT`=100, `PARTIAL`=50, `NOT_FOUND`=0 |
| Purity | `purityPct` | used directly (already 0-100) |
| Condition | `conditionGrade` | `EXCELLENT`=100, `GOOD`=75, `FAIR`=50, `POOR`=25, `SCRAP`=0 |
| Uptime | `workingStatus` | `WORKING`=100, `PARTIAL`=50, `NOT_WORKING`=0 |

```
ahsScore = (presenceScore·presenceWeight + purityScore·purityWeight
            + conditionScore·conditionWeight + uptimeScore·uptimeWeight) / 100
```

**Important — a signal this capture didn't report scores 0 for that component, by design.** Purity/condition/uptime are optional per capture (presence is required on every capture); if one is omitted, it does **not** carry forward the asset's last known value and does **not** get re-normalized out of the average — it counts as 0 and pulls `ahsScore` down. This is intentional: an incomplete capture should visibly lower the score rather than silently coast on stale data.

`GET /api/assets/{id}` also returns `ahsPresenceScore`, `ahsPurityScore`, `ahsConditionScore`, `ahsUptimeScore` (the exact per-component values that fed the last calculation — so `ahsScore` always reconciles against them), `ahsConfidenceLevel` (`HIGH` = all 4 components captured this visit, `MEDIUM` = 3 of 4, `LOW` = ≤2 of 4), and `ahsCalculatedAt` (timestamp of the last capture that recalculated it).

**Not yet implemented:** `ahsStaleFlag`/`ahsStaleSince` (would need a scheduled job to age scores down over time when an asset hasn't been visited recently — there's no scheduler in this backend yet) and `ahsPlFactor` (undefined P&L multiplier, untouched by this engine). Uptime is sourced from the field rep's `workingStatus` observation, not live IoT telemetry — `aiq_iot_telemetry_log` exists in the schema but has no service/API layer yet.

---

## 10. Quick reference table

| Resource | Base path |
|---|---|
| Asset Catalog | `/api/asset-catalog` |
| Vendors | `/api/vendors` |
| Assets (registration, deploy, transfer, swap, history) | `/api/assets` |
| Visit Captures | `/api/visit-captures` |
| Service Events (complaints, work orders) | `/api/service-events` |
| Asset Requests (outlet request → approve from stock) | `/api/asset-requests` |
| Scoring Config (AHS weightage) | `/api/scoring-config` |

Every call needs the `X-Tenant-Id: coca-cola` header. Every response ID is a JSON **string**, not a number.
