#!/bin/bash

curl -X GET --location "http://localhost:8080/shows/c6a7fe4e-5fc0-11eb-ae93-0242ac130002"

curl -X PATCH --location "http://localhost:8080/shows/c6a7fe4e-5fc0-11eb-ae93-0242ac130002/seats/1" \
    -H "Content-Type: application/json" \
    -d "{
          \"action\": \"RESERVE\"
        }"

curl -X PATCH --location "http://localhost:8080/shows/c6a7fe4e-5fc0-11eb-ae93-0242ac130002/seats/1" \
    -H "Content-Type: application/json" \
    -d "{
          \"action\": \"CANCEL_RESERVATION\"
        }"