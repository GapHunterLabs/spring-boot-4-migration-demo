# Migration report: orders-service, Spring Boot 3.5.16 → 4.1.1

Date: 2026-10-19. Java 21, Gradle 9.5.0.

## Result

| | Before | After |
|---|---|---|
| Spring Boot | 3.5.16 | 4.1.1 |
| Jackson | 2.x | 3.1 |
| Tests | 7 of 7 pass | 7 of 7 pass |
| API contract (JSON and CSV, byte for byte) | baseline | identical |

The history is kept step by step, so every finding below can be checked against the commit that shows it:

| Commit | Step | Tests |
|---|---|---|
| `6ecee6b` | Baseline on Spring Boot 3.5.16, with contract tests | 7 / 7 |
| `a18b245` | Add the OpenRewrite Gradle plugin and its Spring Boot 4.0 recipe | — |
| `795464e` | Recipe output, unedited (`UpgradeSpringBoot_4_0`) | does not compile |
| `83264ca` | The smallest change that makes it compile | 3 / 7 |
| `bb3f73c` | Fixes: contract restored, missing dependencies added, recipe leftovers removed | 7 / 7 |
| `f4ddde5` | Spring Boot 4.1.1, migration tooling removed | 7 / 7 |

## What the free recipe handled

OpenRewrite `org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0` (rewrite-spring 6.37.0) did the mechanical
part correctly:

- `spring-boot-starter-web` → `spring-boot-starter-webmvc`, and `flyway-core` → `spring-boot-starter-flyway`. Without
  the Flyway starter, Spring Boot 4 no longer runs the migrations (official migration guide, "Module Dependencies").
- `@MockBean` → `@MockitoBean`, and the test annotations moved to their new packages
  (`org.springframework.boot.webmvc.test.autoconfigure`, `org.springframework.boot.resttestclient`).
- `@AutoConfigureTestRestTemplate` added to the test that uses `TestRestTemplate`.
- Jackson imports moved from `com.fasterxml.jackson` to `tools.jackson`.

There is no community recipe for 4.1 in rewrite-spring 6.37.0, so the step from 4.0.8 to 4.1.1 was done by hand. None
of the properties this service uses changed in 4.1.

## What it left: findings

**Loud** findings show up as a compile or startup failure. **Silent** ones compile, start and answer HTTP 200 with a
different body.

| # | Change in Spring Boot 4 | What happened here | Kind | Fix |
|---|---|---|---|---|
| 1 | Jackson 3 replaces Jackson 2 | The recipe moved the imports to `tools.jackson` but kept Spring's Jackson 2 `Jackson2ObjectMapperBuilder`: `incompatible types: tools.jackson.databind.PropertyNamingStrategy cannot be converted to com.fasterxml.jackson.databind.PropertyNamingStrategy` | Loud (compile) | See #2 |
| 2 | "Defining an `ObjectMapper` bean is no longer sufficient. Define a `JsonMapper`" (migration guide, "Format-Specific Mappers") | The smallest compiling fix keeps an `ObjectMapper` bean. Spring Boot ignores it, so the API silently switches from `customer_name` to `customerName`, and clients that send `customer_name` now get **400** | **Silent** | A `JsonMapperBuilderCustomizer` instead of the bean |
| 3 | `spring.jackson.generator.*` is no longer applied: deprecated with level `error` since 4.0.0, "partially replaced by `spring.jackson.json.write`" (configuration metadata of `spring-boot-jackson` 4.0.8) | The recipe's property migration left `spring.jackson.generator.escape-non-ascii=true` as is, so non-ASCII names (`Café Ñandú`) stopped being escaped for a client that only accepts ASCII | **Silent** | `spring.jackson.json.write.escape-non-ascii=true` |
| 4 | Jackson 3 defaults differ from Jackson 2 | The JSON field order changed (`id` moved from first to last). Jackson 3 sorts properties alphabetically and puts constructor parameters first: with parameter-name detection off, the output becomes alphabetical, and `spring.jackson.use-jackson2-defaults=true` brings the 3.5 order back | **Silent** | `@JsonPropertyOrder` on the entity, so the contract no longer depends on library defaults |
| 5 | `TestRestTemplate` needs `spring-boot-resttestclient` and a runtime `spring-boot-restclient` (migration guide, "TestRestTemplate Failures") | The recipe added the annotation but not `spring-boot-restclient`: `NoClassDefFoundError: org/springframework/boot/restclient/RestTemplateBuilder`, context fails to load | Loud (test startup) | Add the dependency |
| 6 | — (recipe leftover) | The recipe added `runtimeOnly("javax.xml.bind:jaxb-api:2.3.1")`, which this service never used, and pinned `spring-boot-starter-webmvc-test:4.0.8` instead of leaving the version to the BOM | Noise | Both removed |

**Expected but not reproduced:** the migration guide says contributing `HttpMessageConverter` beans "is not supported
anymore". This service's CSV converter is a plain `@Component` and kept working on 4.0.8 and 4.1.1: the CSV contract
test passes unchanged.

## Why contract tests

Findings 3 and 4 were caught only by the byte-for-byte contract test. Finding 2 was also caught by one validation test
that sends snake_case. Every other test was green. Without the contract test, these changes would have reached the
clients.

## Reproduce

```
git log --oneline
git diff 6ecee6b f4ddde5            # the whole migration
git checkout 795464e && ./gradlew test   # recipe output: does not compile
git checkout 83264ca && ./gradlew test   # compiles; 4 of 7 tests fail
git checkout f4ddde5 && ./gradlew test   # 7 of 7
```

When a contract test fails, the body actually received is written to `build/contract-actual/`.

## Sources

- Spring Boot 4.0 Migration Guide (Spring Boot wiki), sections "Module Dependencies", "Upgrading Jackson",
  "HttpMessageConverters Deprecation", "Upgrading Testing Features".
- Spring Boot 4.1.1 system requirements: Java 17 to 26, Gradle 8.14+ or 9.x.
- OpenRewrite: Gradle plugin 7.39.0, `rewrite-recipe-bom` 3.37.0. Versions 7.40.0 and 7.41.0 of the plugin could not
  be used on 2026-10-19: they depend on `rewrite-bom` 8.91.0, which was not on Maven Central yet.
