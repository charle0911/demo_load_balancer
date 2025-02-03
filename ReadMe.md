# Loadbalancer
## Test script (CURL)
curl -s -o /dev/null -D - -X POST http://localhost:8090/api/route \
-H "Content-Type: application/json" \
-d '{"game":"Mobile Legends", "gamerID":"GYUTDTE", "points":20}'