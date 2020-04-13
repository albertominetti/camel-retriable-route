Camel retriable route with time conditions
=========================

Goal of the project: create a route that uses a downstream provider, if the provider end-point fails with a `404` and a response time is below 1 second retries 2 times, otherwise fail.

To build this project use

    mvn compile

To run this project use

    mvn exec:java 
     
     
Test with the following urls:

    http://localhost:8080/api/trigger/{{parameter}}

the {{parameter}} describe the behaviour of the mocked provider, included in the route WebRouteBuilder, possible values are:
* `always-404`
* `404-404-200`
* `always-200`
* `slow-200`
* `slow-404`

Example: `http://localhost:8080/api/trigger/404-404-200`

For more help see the Apache Camel documentation

    http://camel.apache.org/



