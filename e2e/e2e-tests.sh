#!/bin/bash

# Check if mvn is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven (mvn) is not installed. Please install it and try again."
    exit 1
fi

# Check if k6 is installed
if ! command -v k6 &> /dev/null; then
    echo "k6 is not installed. Please install it and try again."
    exit 1
fi

# Run Maven clean install to build the project
echo "Running mvn clean install..."
mvn clean install

# Check if the jar file was created successfully
if [ ! -f target/quarkus-app/quarkus-run.jar ]; then
    echo "Jar file not found! Build may have failed."
    exit 1
fi

# Start the application
echo "Starting the application..."
java -jar target/quarkus-app/quarkus-run.jar > service.log &
SERVICE_PROCESS=$!

# Wait for the application to start
tail -f -n0 service.log | grep -q 'Listening on'
echo "Application started"

# Run k6 e2e tests
echo "Running k6 e2e tests..."
k6 run ./e2e/api-test.js

# Stop the application after tests complete
kill $SERVICE_PROCESS
rm service.log

echo "Tests completed, application stopped."
