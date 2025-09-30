# Use a lightweight base image with Java 21 JRE
FROM bellsoft/liberica-runtime-container:jre-21-slim-musl

# Set the working directory inside the container
WORKDIR /application

# Copy the Jar file
COPY target/*-SNAPSHOT.jar app.jar

# create a non-root user (Alpine / musl)
RUN addgroup -S credentialservice && adduser -S -G credentialservice credentialservice

# Switch to non-root user
USER credentialservice

# Expose the port that the application will run on
EXPOSE 8140

# Set JVM options for optimal container performance
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the application
CMD ["java", "-jar", "app.jar"]
