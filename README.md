# axon5-simple — Axon 4/5 cross-version query compatibility test

## Purpose

This project reproduces a cross-version incompatibility between an **Axon Framework 5** query handler
and an **Axon Framework 4** query client when the handler returns an **empty `Optional`** (i.e. the
queried resource does not exist).

## Applications

| App | Axon version | Role |
|-----|-------------|------|
| `server5` | 5.2.0 | `@QueryHandler` returning `Optional<CustomerDto>` |
| `client5` | 5.2.0 | queries `server5`, works correctly |
| `client4` | 4.13.2 | queries `server5`, fails on empty result |

All three apps are Spring Boot + Kotlin and connect to AxonServer (see `docker-compose.yaml`).

## How to run

```bash
docker compose up -d          # start AxonServer
./mvnw spring-boot:run -pl server5
./mvnw spring-boot:run -pl client4   # or client5
```

Trigger the happy path (customer exists):

```bash
curl http://localhost:8083/customer/1   # returns 200 with CustomerDto
```

Trigger the failing path (customer does not exist):

```bash
curl http://localhost:8083/customer/11  # should return 404, but client4 returns 500
```

## Observed behaviour

### `client5` (Axon 5) — works

Both found and not-found customers return the expected HTTP status.

### `client4` (Axon 4) — broken for missing customers

```
500 Internal Server Error
AxonServerQueryDispatchException: Query did not yield the expected number of results.
```

Full stack trace originates from `AxonServerQueryBus$ResponseProcessingTask.run()` inside
`axon-server-connector-4.13.2.jar`.

## Root cause

The failure is a **behavioral change** in Axon 5's messaging layer that is incompatible with the Axon 4
client's wire-level expectation.

### Step 1 — Axon 5 server: empty `Optional` → empty `MessageStream` (zero messages)

`MessageStreamResolverUtils.resolveToStream()` in `axon-messaging-5.2.0`:

```java
if (result == null) { return MessageStream.empty(); }
...
case Optional<?> optional when optional.isPresent() ->
        MessageStream.just(new GenericMessage(typeResolver.resolveOrThrow(r), r));
case Optional<?> empty -> MessageStream.empty();   // ← empty Optional = ZERO items on the wire
```

### Step 2 — Axon 5 server connector: empty stream closes without sending a `QueryResponse`

`FlowControlledResponseSender.sendResponses()` in `axon-server-connector-5.2.0`:
when the upstream stream is empty and completed it calls `downstream::complete` without ever writing
a `QueryResponse` message. No error is logged on the server side because nothing errored.

### Step 3 — AxonServer relays the normally-completed stream

Zero `QueryResponse` messages are forwarded to the client.

### Step 4 — Axon 4 client: zero responses on a closed stream is an error

`AxonServerQueryBus$ResponseProcessingTask.run()` in `axon-server-connector-4.13.2`:

```java
QueryResponse nextAvailable = result.nextIfAvailable();
if (nextAvailable != null) { /* complete the future */ }
else if (result.isClosed() && !queryTransaction.isDone()) {
    throw new AxonServerQueryDispatchException(
            ..., "Query did not yield the expected number of results.");
}
```

The direct-query protocol in Axon 4 requires **at least one** `QueryResponse` message. A cleanly
closed stream with zero messages is considered an error.

## Why this worked in an all-Axon-4 setup

`SimpleQueryBus.buildCompletableFuture()` always calls
`GenericQueryResponseMessage.asNullableResponseMessage(declaredType, result)`.
For a `null`/empty result this produces **one** `QueryResponseMessage` with a **null payload** and the
declared return type, which is serialized into **one** `QueryResponse` on the wire.
The Axon 4 client completes the future with `null`, the controller's `defaultIfEmpty(...)` maps that to
HTTP 404. The "zero responses" situation never occurred in a homogeneous Axon 4 deployment.

## There is no client-side workaround

Switching the response type from `instanceOf` to `optionalInstanceOf` does not help. The
`ResponseProcessingTask` is always used for direct (point-to-point) queries and errors on zero
responses regardless of the declared response type. The behavioral difference originates on the Axon 5
server.

## Environment

| Component | Version |
|-----------|---------|
| Axon Framework (server) | 5.2.0 (`io.axoniq.framework`) |
| Axon Framework (client4) | 4.13.2 |
| Spring Boot | 4.1.0 (server5 / client5), 3.x (client4) |
| AxonServer | 2026.0.0 |
| Serializer | Jackson |
| Language | Kotlin |

## Relevant source files

| File | Role |
|------|------|
| `server5/src/main/kotlin/org/axonframework/simple/CustomerQueryHandler.kt` | query handler returning `Optional<CustomerDto>` |
| `server5/src/main/kotlin/org/axonframework/simple/models.kt` | `CustomerFindOneQuery`, `CustomerDto` |
| `client4/src/main/kotlin/org/axonframework/simple/CustomerController.kt` | Axon 4 client issuing the query |
| `client5/src/main/kotlin/org/axonframework/simple/CustomerController.kt` | Axon 5 client (works correctly) |
