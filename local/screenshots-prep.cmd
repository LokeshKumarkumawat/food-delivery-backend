@echo off
cls
echo ===== 01 cluster overview =====
kubectl get nodes -o wide
helm list -n foodapp
kubectl get deploy,pods,svc,hpa,pdb,ingress,netpol -n foodapp
echo.
pause
cls
echo ===== 02 helm history =====
helm history foodapp -n foodapp
echo.
pause
cls
echo ===== 06 hpa and storage =====
kubectl get hpa -n foodapp
kubectl top pods -n foodapp
kubectl get pvc -n foodapp
echo.
pause
cls
echo ===== 07 ingress =====
curl -i -H "Host: api.foodapp.localhost" http://localhost/api/categories/all
echo.
pause
cls
echo ===== 09 startup and health =====
kubectl logs deploy/foodapp -n foodapp | findstr /i /c:"profile is active" /c:"Successfully validated" /c:"up to date" /c:"Started Food"
kubectl exec deploy/foodapp -n foodapp -- curl -s http://localhost:9090/actuator/health/readiness
echo.
pause
cls
echo ===== 10 docker image =====
docker images food-delivery-backend
echo.
pause