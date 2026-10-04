# Emergent POS — Spring Boot Backend

A complete, independent re-implementation of the `backend/` (FastAPI) service in **Java 17 + Spring Boot 3**. It exposes the exact same HTTP API contract, so the existing React web app, the WebView Android app, and the native Kotlin Android app (`android-native/`) can all talk to this backend **with zero client-side changes** — just point them at wherever this server runs instead of the FastAPI one.

This backend defaults to **port 8081** (the FastAPI backend defaults to 8000), so both can run at the same time on one machine, each backed by its own native Android app (`android-native/` -> FastAPI:8000, `android-native-springboot/` -> Spring Boot:8081) — or point either app at either backend's port, since both expose the identical API. Both talk to the same Postgres database/tables, so data is shared regardless of which backend(s) you run.

## Why this exists / how it works

Every client (web + both Android apps) actually persists **all** of its data — items, parties, invoices, purchase invoices, ledger entries, bank accounts, cash entries, estimates, shop profile, settings, everything — through one generic key/value store:

- `GET /api/config/{key}` — read a JSON blob previously saved under `key` (e.g. `hw_items`)
- `POST /api/config` — upsert a JSON blob under a key (body: `{"config_key": "...", "config_value": "...json string..."}`)

This is the **real** read/write path and the only one any client relies on. On top of that, exactly like the FastAPI backend, every write to one of the 8 known entity keys (`hw_items`, `hw_parties`, `hw_invoices`, `hw_purchaseInvoices`, `hw_ledgerEntries`, `hw_bankAccounts`, `hw_cashEntries`, `hw_estimates`) is additionally mirrored into a normal relational table (`items`, `parties`, `invoices`, ...) so you can browse/query the data directly in pgAdmin/psql. Those tables are **read-only from the API's perspective** — they're fully rebuilt from `app_config` on every relevant write and once again at startup (`ConfigSyncService`), so `app_config` remains the single source of truth. Column names in the mirror tables intentionally match the apps' camelCase JSON fields 1:1 (e.g. `retailPrice`, not `retail_price`) to match the existing FastAPI-created schema exactly.

## Endpoints (parity with `backend/main.py`)

| Method | Path | Notes |
|---|---|---|
| GET | `/health` | liveness check |
| GET | `/api/config/{key}` | 404 if never saved |
| POST | `/api/config` | upsert; body `{config_key, config_value}` |
| GET | `/api/shop-profile` | legacy REST resource, unused by current clients (they use `/api/config` key `hw_shopProfile` instead) |
| POST | `/api/shop-profile` | same |
| GET | `/api/items` | read-only mirror |
| GET | `/api/parties` | read-only mirror |
| GET | `/api/invoices` | read-only mirror |
| GET | `/api/purchase-invoices` | read-only mirror |
| GET | `/api/ledger-entries?party_id=` | read-only mirror, optional filter |
| GET | `/api/bank-accounts` | read-only mirror |
| GET | `/api/cash-entries` | read-only mirror |
| GET | `/api/estimates` | read-only mirror |

## Project layout

```
springboot-backend/
├── build.gradle.kts             # Spring Boot 3.3.4, Java 17, Postgres driver, Lombok
├── src/main/java/com/emergent/pos/
│   ├── EmergentPosApplication.java
│   ├── config/WebConfig.java        # CORS (mirrors CORS_ORIGINS env var)
│   ├── model/                       # JPA entities: AppConfig, ShopProfile + 8 mirror entities
│   ├── repository/                  # Spring Data JPA repositories
│   ├── service/ConfigSyncService.java  # JSON blob -> mirror table sync (+ startup backfill)
│   ├── dto/ConfigRequest.java
│   └── controller/                  # HealthController, ConfigController, ShopProfileController, MirrorDataController
└── src/main/resources/application.properties
```

## Prerequisites

- **Java 17** (JDK) — same as the native Android app build
- The same **Postgres** database the FastAPI backend uses (default: `emergent_db` on `localhost:5432`, user/pass `postgres`/`postgres`), OR any fresh Postgres instance if you want to run this backend standalone (`spring.jpa.hibernate.ddl-auto=update` will create all tables automatically on first run).
- No local Maven/Gradle install needed — this project ships a Gradle wrapper (`gradlew`/`gradlew.bat`); it downloads Gradle itself on first run.

## Running it

From `springboot-backend/`:

```powershell
.\gradlew.bat bootRun
```

(macOS/Linux: `./gradlew bootRun`)

The API starts on **http://localhost:8081** by default (the FastAPI backend uses 8000) — this lets both backends run at the same time. To point it at a different database or port, set environment variables before starting (Spring Boot's standard names):

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/emergent_db"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "postgres"
$env:SERVER_PORT = "8081"
$env:CORS_ORIGINS = "*"
.\gradlew.bat bootRun
```

> Note: `SPRING_DATASOURCE_URL` needs the JDBC scheme (`jdbc:postgresql://...`), not the `postgresql://...` scheme used by `backend/.env`'s `DATABASE_URL` — copy the host/port/db name but keep the `jdbc:` prefix.

### Building a runnable jar

```powershell
.\gradlew.bat bootJar
java -jar build\libs\emergent-pos-backend-1.0.0.jar
```

## Verifying it against your existing data

Because this backend talks to the **same Postgres tables** (`app_config` etc.) as the FastAPI backend, you can freely switch between the two — stop one, start the other, same data, same clients, no migration needed. A quick check:

```powershell
curl http://localhost:8081/health
curl http://localhost:8081/api/config/hw_shopProfile
```

## Known differences from the FastAPI backend

- Error response bodies use Spring's default `application/problem+json` shape instead of FastAPI's `{"detail": "..."}` — none of the current clients inspect error bodies (they only check the HTTP status code), so this doesn't affect behavior.
- The upsert in `POST /api/config` is a plain find-or-create-then-save (not a single atomic `INSERT ... ON CONFLICT`). This is safe for this app's actual usage pattern (one shop, effectively single-writer) but isn't linearizable under heavy concurrent writes to the same key the way the FastAPI backend's Postgres-native upsert is.
