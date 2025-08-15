#!/bin/bash

set -e

./mvnw clean package -DskipTests

docker build -t moderegistry:latest -f Dockerfile .

docker build -t custom-mysql:latest -f Dockerfile.mysql .

docker build -t custom-minio:latest -f Dockerfile.minio .

docker network create modelvault-net || true

docker rm -f mysql-container || true
docker rm -f minio-container || true
docker rm -f moderegistry-container || true

docker run -d \
  --name mysql-container \
  --network modelvault-net \
  -p 3306:3306 \
  custom-mysql:latest

docker run -d \
  --name minio-container \
  --network modelvault-net \
  -p 9000:9000 \
  -p 9001:9001 \
  custom-minio:latest

docker run -d \
  --name moderegistry-container \
  --network modelvault-net \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-container:3306/modelvault?useSSL=false&serverTimezone=UTC \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=yourpassword \
  -e MINIO_URL=http://minio-container:9000 \
  -e MINIO_ACCESS-KEY=minioadmin \
  -e MINIO_SECRET-KEY=minioadmin \
  moderegistry:latest