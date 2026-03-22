# Use the official OpenJDK 17 image (Alpine version for smaller size)
FROM eclipse-temurin:17-jdk-alpine

# Create and set the working directory
WORKDIR /app

# Copy the server source code and any necessary dependencies
COPY Server.java .

# Compile the Java server
RUN javac Server.java

# Expose the communication port (80)
EXPOSE 80

# Start the server when the container launches
CMD ["java", "Server"]
