@echo off
setlocal enabledelayedexpansion
rem Usage: local\zero-downtime.cmd [seconds] [delete_at_second]
set DUR=%1
if "%DUR%"=="" set DUR=40
set KILLAT=%2
if "%KILLAT%"=="" set KILLAT=8
set OK=0
set FAIL=0

echo === Zero-downtime test: %DUR%s, 3 endpoints per second, 1 foodapp pod deleted at %KILLAT%s
kubectl rollout status deploy/foodapp -n foodapp --timeout=120s >nul
kubectl get pods -n foodapp -l app.kubernetes.io/name=foodapp
echo.

for /l %%s in (1,1,%DUR%) do (
  if %%s==%KILLAT% (
    kubectl get pods -n foodapp -l app.kubernetes.io/name=foodapp -o name > "%TEMP%\zd-pods.txt"
    set /p VICTIM=<"%TEMP%\zd-pods.txt"
    echo   --- DELETING !VICTIM! ---
    kubectl delete !VICTIM! -n foodapp --wait=false >nul
  )
  call :hit /api/categories/all
  set C1=!CODE!
  call :hit /api/menu
  set C2=!CODE!
  call :hit /api/reviews/menu-item/1
  set C3=!CODE!
  echo [%%ss] categories=!C1! menu=!C2! reviews=!C3!   ok=!OK! fail=!FAIL!
  timeout /t 1 /nobreak >nul
)

set /a TOTAL=OK+FAIL
echo.
echo === RESULT: !TOTAL! requests, !OK! returned 200, !FAIL! did not
echo.
kubectl get pods -n foodapp -l app.kubernetes.io/name=foodapp
exit /b 0

:hit
set CODE=000
curl -s -o NUL -m 3 -w "%%{http_code}" -H "Host: api.foodapp.localhost" http://localhost%1 > "%TEMP%\zd-code.txt" 2>nul
set /p CODE=<"%TEMP%\zd-code.txt"
if "!CODE!"=="200" (set /a OK+=1) else (set /a FAIL+=1)
goto :eof