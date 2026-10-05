@echo off
setlocal
for /f %%i in ('git rev-parse --short HEAD') do set GIT_SHA=%%i
if "%GIT_SHA%"=="" (echo ERROR: could not read git commit & exit /b 1)
for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMddHHmmss"') do set TS=%%i
set TAG=%TS%-%GIT_SHA%
echo === image tag: %TAG%

docker build -t food-delivery-backend:%TAG% . || exit /b 1
kind load docker-image food-delivery-backend:%TAG% --name foodapp || exit /b 1

helm upgrade --install foodapp helm\foodapp -n foodapp -f helm\foodapp\values-local.yaml --set image.tag=%TAG% --atomic --timeout 5m || exit /b 1

kubectl -n foodapp rollout status deploy/foodapp --timeout=300s || exit /b 1
kubectl -n foodapp exec deploy/foodapp -- curl -fsS http://localhost:9090/actuator/health/readiness || exit /b 1
echo.
echo === DEPLOYED %TAG%