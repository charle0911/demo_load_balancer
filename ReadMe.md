# Loadbalancer
## Requirement
- How would my round robin API handle it if one of the application APIs goes down?
- How would my round robin API handle it if one of the application APIs starts to go
slowly?
- How would I test this application
## Introduction
### Health Check

- How would my round robin API handle it if one of the application APIs goes down?

Using the health check mechanism to solve the
The loadbalancer will send the heart beat request to each application by x sec. 

### Probation Check
- How would my round robin API handle it if one of the application APIs starts to go

If the response time is longer than threshold then system will put it into the probation list.
And use the lower frequency to check the endpoint to wait the application machine cool down.


### Test
- How would I test this application

Use the Intellij import the whole maven project

For loadbalancer, running the main function in the Application.class.

For application, running the spring boot by different profile.
- The profile one and two is the normal application
- The profile three is the mock timeout application. (For testing slow response)

The response to client will be added the src attribute in the header. To make us to check where the response come from



## Test script (CURL)

### Request
``` sh
curl --raw -s -D - -X POST http://localhost:8090/api/route \
-H "Content-Type: application/json" \
-d '{"game":"Mobile Legends", "gamerID":"GYUTDTE", "points":20}'
```

### Response
``` http request
HTTP/1.1 200 OK
Src: http://localhost:8081/api/route
Date: Mon, 03 Feb 2025 10:47:09 GMT
Content-length: 57

{"game":"Mobile Legends","gamerID":"GYUTDTE","points":20}%        
```