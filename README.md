# axon5-simple — Axon 4/5 cross-version query compatibility test

## Purpose

This project reproduces a cross-version incompatibility between **Axon Framework 5** query handler and an **Axon Framework 4** query client.

## Applications

| App       | Axon version | Role                                  |
|-----------|--------------|---------------------------------------|
| `client4` | 4.13.2       | dispatches queries                    |
| `server4` | 4.13.2       | provides `@QueryHandler`s using Axon4 |
| `client5` | 5.3.1        | dispatches queries                    |
| `server5` | 5.3.1        | provides `@QueryHandler`s using Axon5 |

All apps are Spring Boot + Kotlin and connect to AxonServer (see `docker-compose.yaml`).

## Tests using AF4 client against AF5 server
```bash
docker compose up -d          # start AxonServer
./mvnw spring-boot:run -pl server5
./mvnw spring-boot:run -pl client4

# streamingQuery works
curl  http://localhost:8083/customer/streaming   
[{"customerId":"1","name":"Alice"},{"customerId":"2","name":"Bob"},{"customerId":"3","name":"Charlie"}]
    
# subscriptionQuery fails
curl  http://localhost:8083/customer/subscription
{"timestamp":1788363218715,"path":"/customer/subscription","status":500,"error":"Internal Server Error","requestId":"ab249c68-3"}    

# normal query works
curl  http://localhost:8083/customer
[{"customerId":"1","name":"Alice"},{"customerId":"2","name":"Bob"},{"customerId":"3","name":"Charlie"}]
```

## Tests using AF5 client against AF5 server
```bash
docker compose up -d          # start AxonServer
./mvnw spring-boot:run -pl server5
./mvnw spring-boot:run -pl client5

# streamingQuery works
curl  http://localhost:8082/customer/streaming   
[{"customerId":"1","name":"Alice"},{"customerId":"2","name":"Bob"},{"customerId":"3","name":"Charlie"}]
    
# subscriptionQuery fails
curl  http://localhost:8082/customer/subscription
{"timestamp":1788363218715,"path":"/customer/subscription","status":500,"error":"Internal Server Error","requestId":"ab249c68-3"}    

# normal query works
curl  http://localhost:8082/customer
[{"customerId":"1","name":"Alice"},{"customerId":"2","name":"Bob"},{"customerId":"3","name":"Charlie"}]
```
