#!/bin/bash

echo "Starting docker compose..."
docker compose up --wait

echo "Creating tables in postgres..."
docker exec -i postgres-db psql -U postgres -t < ./experiments/src/main/resources/create-tables-postgres.sql

echo "Docker containers status..."
docker ps

echo
echo "Type "
echo
echo "  docker exec -it postgres-db psql -U postgres"
echo
echo "to attach to the postgres container and run queries"
