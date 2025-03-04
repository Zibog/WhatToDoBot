# Use Amazon Corretto 21 runtime as a parent image
FROM amazoncorretto:21-alpine

# Copy the source code to the container
COPY . /src

# Set the working directory in the container
WORKDIR /src

# Make ./gradlew executable
#RUN chmod +x ./gradlew

# Build the application using Gradle Wrapper
#RUN ./gradlew --no-daemon build

# Copy built files to the /app directory
COPY /build /app

# Set the working directory in the container to /app
WORKDIR /app

# Run the application
RUN ./install/WhatToDoBot/bin/WhatToDoBot