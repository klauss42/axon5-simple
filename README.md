# axon5-simple — Axon 4/5 cross-version query compatibility test

## Purpose

This project reproduces a cross-version incompatibility between an **Axon Framework 5** query handler and an **Axon Framework 4** query
client when the handler returns a list or Page.

## Applications

| App       | Axon version | Role                                              |
|-----------|--------------|---------------------------------------------------|
| `server5` | 5.2.2        | `@QueryHandler` returning `Optional<CustomerDto>` |
| `client5` | 5.2.2        | queries `server5`, works correctly                |
| `client4` | 4.13.2       | queries `server5`, but returns wrong result       |

All three apps are Spring Boot + Kotlin and connect to AxonServer (see `docker-compose.yaml`).

## Query lists

```bash
docker compose up -d          # start AxonServer
./mvnw spring-boot:run -pl server5
./mvnw spring-boot:run -pl client4
./mvnw spring-boot:run -pl client5
```

### AF5 client
```bash
curl  http://localhost:8082/customer
[{"customerId":"1","name":"Alice"},{"customerId":"2","name":"Bob"},{"customerId":"3","name":"Charlie"}]%

curl  http://localhost:8082/customer/page
[{"customerId":"1","name":"Alice"},{"customerId":"2","name":"Bob"},{"customerId":"3","name":"Charlie"}]%
```

### AF4 client

```bash
curl  http://localhost:8083/customer
[{"customerId":"1","name":"Alice"}]%

curl  http://localhost:8083/customer/page
[{"customerId":"1","name":"Alice"}]%

```

