# AssetIQ Backend — Environment Variables

For devops setting up deployment. These are the environment variables the
service reads at startup, sourced from `application.yml`,
`application-mysql.yml`, and `SnowflakeIdWorker`.

---

## Variables

| Variable | Required? | Default | Notes |
|---|---|---|---|
| `SERVER_PORT` | No | `8080` | Pick whatever port is free in the target environment. |
| `DB_HOST` | Yes (prod) | `localhost` | |
| `DB_PORT` | No | `3306` | |
| `DB_NAME` | Yes (prod) | `assetiq` | |
| `DB_USERNAME` | Yes (prod) | `assetiq` | |
| `DB_PASSWORD` | Yes (prod) | `assetiq` | Dev-only placeholder — production must use a real, generated secret. |
| `SNOWFLAKE_WORKER_ID` | **Yes, if running >1 instance** | `0` | Unique per running instance (0–1023, 10-bit field). Primary keys are generated in-app as Snowflake-style IDs; two instances sharing a worker ID can collide within the same millisecond. Single-instance deployments can leave this at the default. |

## Notes for devops

- The `assetiq`/`assetiq` defaults baked into the YAML are dev conveniences,
  not real credentials — override `DB_HOST`, `DB_NAME`, `DB_USERNAME`, and
  `DB_PASSWORD` explicitly per environment.
- `SNOWFLAKE_WORKER_ID` is easy to miss since it isn't set in any YAML file —
  it's read directly via `System.getenv` in
  `SnowflakeIdWorker`. Critical for any deployment running more than one
  replica.
- There is no Dockerfile or Kubernetes manifest in this repo yet. Build with
  `mvn package` (Java 17) to produce `target/AssetAI-1.0-SNAPSHOT.jar`, then
  run it with `java -jar target/AssetAI-1.0-SNAPSHOT.jar`.
- Liquibase manages the schema on startup (`ddl-auto: validate` — the app
  never generates/alters DDL itself), so the target database just needs to
  exist and be reachable; migrations run automatically on boot.
