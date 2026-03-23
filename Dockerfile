FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Install system OpenCV runtime libraries
RUN apt-get update && apt-get install -y --no-install-recommends \
    libopencv-dev \
    libgtk2.0-0 \
    libglib2.0-0 \
    libsm6 \
    libxrender1 \
    libxext6 \
  && rm -rf /var/lib/apt/lists/*

# Ensure JVM can find OpenCV native libraries
ENV JAVA_TOOL_OPTIONS="-Djava.library.path=/usr/lib/x86_64-linux-gnu"

COPY --from=build /app/target/hr-system-0.0.1-SNAPSHOT.jar /app/hr-system.jar
EXPOSE 18080
ENTRYPOINT ["java","-jar","/app/hr-system.jar"]
