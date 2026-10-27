FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY gateway/pom.xml gateway/
COPY customer/pom.xml customer/
COPY ticket/pom.xml ticket/
RUN mvn dependency:go-offline -B
COPY customer/src ./customer/src
RUN mvn -pl customer -am package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/customer/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
