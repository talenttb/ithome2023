FROM litestream/litestream as litestream

FROM clojure:temurin-17-tools-deps-jammy as builder
RUN apt update && apt install -y ca-certificates sqlite3 libsqlite3-dev rlwrap wget curl && rm -rf /var/lib/apt/lists/*
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN clj -T:build uber

FROM eclipse-temurin:17-jammy
RUN apt update && apt install -y ca-certificates sqlite3 libsqlite3-dev rlwrap wget curl && rm -rf /var/lib/apt/lists/*
WORKDIR /usr/src/app
COPY . /usr/src/app
COPY --from=litestream /usr/local/bin/litestream /usr/local/bin/litestream
COPY --from=builder /usr/src/app/target/app.jar /usr/src/app/target/app.jar

ENTRYPOINT ["java", "-jar", "target/app.jar"]
