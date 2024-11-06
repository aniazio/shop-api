#!/bin/bash

# URL of the endpoint you want to test (adjust as necessary)
URL="http://localhost:8080/users/login"


# 4. Send POST requests with CSRF token and session cookie
for i in {1..30}; do
  echo "Sending PUT Request #$i:"
  curl -X PUT "$URL" \
       -H "Content-Type: application/json" \
       -d '{"email" : "my@mail.com", "password" : "123gsd$"}' \
       -w "\nHTTP Code: %{http_code}\n"
  echo ""
done
