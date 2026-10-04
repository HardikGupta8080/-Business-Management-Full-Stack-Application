# Business Management Full-Stack Application

A shop / POS system for billing, inventory, parties, purchases, GST, banking, cash, and estimates. This repository contains a **Spring Boot** REST API and a **native Android (Jetpack Compose)** client that share the same Postgres data.

## Features

- **Billing** — create sales invoices from inventory
- **Inventory** — items, stock, and pricing
- **Parties** — customers and suppliers
- **Purchases** — purchase invoices
- **Ledger** — party-wise credit / debit history
- **Sales history** — past invoices
- **GST register** — tax-oriented summaries
- **Bank accounts** and **cash collections**
- **Estimates** — quotations before converting to sales
- **AI assistant** screen in the Android app
- **Settings** — shop profile and backend host/port (no rebuild needed when the PC IP changes)

## Tech stack

| Layer | Stack |
| --- | --- |
| Backend | Java 17, Spring Boot 3.3, Spring Data JPA, PostgreSQL |
| Android | Kotlin, Jetpack Compose, Navigation Compose, OkHttp |
| Build | Gradle Wrapper (backend and Android) |

Default API port: **8081**. Default database: `emergent_db` on `localhost:5432` (`postgres` / `postgres`).

## Repository layout

```
.
├── backend/     # Spring Boot API (com.emergent.pos)
└── frontend/    # Android app (com.emergent.posapp.springboot)
```

## How data is stored

Clients persist almost everything through a key/value config API:

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/health` | Liveness check |
| `GET` | `/api/config/{key}` | Read a JSON blob (404 if missing) |
| `POST` | `/api/config` | Upsert `{ "config_key", "config_value" }` |

Known keys include `hw_items`, `hw_parties`, `hw_invoices`, `hw_purchaseInvoices`, `hw_ledgerEntries`, `hw_bankAccounts`, `hw_cashEntries`, `hw_estimates`, and `hw_shopProfile`.

Writes to those entity keys are also mirrored into relational tables (`items`, `parties`, `invoices`, …) so you can inspect data in pgAdmin or `psql`. `app_config` remains the source of truth; mirror tables are rebuilt from it.

Read-only mirror endpoints: `/api/items`, `/api/parties`, `/api/invoices`, `/api/purchase-invoices`, `/api/ledger-entries`, `/api/bank-accounts`, `/api/cash-entries`, `/api/estimates`. Shop profile also exists at `/api/shop-profile`; the Android app uses the config key instead.

## Prerequisites

- **JDK 17**
- **PostgreSQL** with a database named `emergent_db` (or change the JDBC URL)
- **Android Studio** (or Android SDK) to build the app
- Phone/emulator on the **same network** as the backend, or use `10.0.2.2` for the emulator

No global Gradle install is required; both projects ship `gradlew` / `gradlew.bat`.

## Run the backend

```powershell
cd backend
.\gradlew.bat bootRun
```

macOS / Linux:

```bash
cd backend
./gradlew bootRun
```

API: [http://localhost:8081](http://localhost:8081)

Optional environment variables:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/emergent_db"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "postgres"
$env:SERVER_PORT = "8081"
$env:CORS_ORIGINS = "*"
.\gradlew.bat bootRun
```

Use the JDBC scheme (`jdbc:postgresql://...`), not `postgresql://`.

Runnable jar:

```powershell
cd backend
.\gradlew.bat bootJar
java -jar build\libs\emergent-pos-backend-1.0.0.jar
```

Smoke test:

```powershell
curl http://localhost:8081/health
```

## Run the Android app

1. Open the `frontend/` folder in Android Studio.
2. Start the backend on your PC.
3. Install the app on a device (`minSdk` 24).
4. In **Settings**, set the server host to your PC’s LAN IP (default in code is `192.168.1.45`) and port `8081`.

The app talks to `http://<host>:<port>/api/config/...`. Allow HTTP cleartext on the LAN; the project includes a network security config for that.

## License

Use and modify this project as you need for your own shop or coursework unless you add a different license file.
