@echo off
setlocal

echo Cleaning and packaging Maven project...
call mvnw clean package -DskipTests
if %errorlevel% neq 0 (
    echo Maven build failed!
    exit /b %errorlevel%
)

echo Building Docker images...
docker build -t moderegistry:latest -f Dockerfile .
docker build -t custom-mysql:latest -f Dockerfile.mysql .
docker build -t custom-minio:latest -f Dockerfile.minio .

echo Creating network...
docker network create modelvault-net >nul 2>&1

echo Removing existing containers if they exist...
docker rm -f mysql-container >nul 2>&1
docker rm -f minio-container >nul 2>&1
docker rm -f moderegistry-container >nul 2>&1

echo Starting MySQL container...
docker run -d ^
  --name mysql-container ^
  --network modelvault-net ^
  -p 3306:3306 ^
  custom-mysql:latest

echo Starting MinIO container...
docker run -d ^
  --name minio-container ^
  --network modelvault-net ^
  -p 9000:9000 ^
  -p 9001:9001 ^
  custom-minio:latest

echo Starting moderegistry container...
docker run -d ^
  --name moderegistry-container ^
  --network modelvault-net ^
  -p 8080:8080 ^
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-container:3306/modelvault?useSSL=false&serverTimezone=UTC ^
  -e SPRING_DATASOURCE_USERNAME=root ^
  -e SPRING_DATASOURCE_PASSWORD=yourpassword ^
  -e MINIO_URL=http://minio-container:9000 ^
  -e MINIO_ACCESS-KEY=minioadmin ^
  -e MINIO_SECRET-KEY=minioadmin ^
  moderegistry:latest

echo All containers started successfully!
echo   - App: http://localhost:8080
echo   - Swagger: http://localhost:8080/swagger-ui.html
echo   - MinIO Console: http://localhost:9001