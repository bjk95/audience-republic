# Stage 1: Build the application using sbtscala/scala-sbt with sbt 1.10.7
# Tag format: <JDK version>_<sbt version>_<Scala version>
FROM sbtscala/scala-sbt:eclipse-temurin-23.0.1_11_1.10.7_2.13.16 AS builder
WORKDIR /app
# Copy all project files into the container
COPY . .
# Build the fat jar using sbt-assembly
RUN sbt clean assembly

# Stage 2: Create the final, slim runtime image
FROM eclipse-temurin:11-jre
WORKDIR /app
# Adjust the path to your built JAR if needed.
COPY --from=builder /app/target/scala-2.13/audience-republic-assembly-0.1.0-SNAPSHOT.jar /app/app.jar
# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
