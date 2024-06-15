# Use the official Gradle image to build the app
FROM gradle:jdk17 AS build
LABEL authors="alexcastrodev"

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy the rest of the application source code
COPY src ./src

# Build the application
RUN ./gradlew build

# Use the official OpenJDK image to run the app
FROM openjdk:17

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/storing-data.jar app.jar

# Specify the command to run the application
CMD ["java", "-jar", "app.jar"]