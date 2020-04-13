package org.example;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMethods;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.apache.camel.support.builder.PredicateBuilder.and;
import static org.apache.camel.support.builder.PredicateBuilder.not;


public class MainRouteBuilder extends RouteBuilder {

    public void configure() {
        getContext().setMessageHistory(false);

        restConfiguration().component("jetty").host("localhost").port("8080");

        onException(RetriableException.class)
                .maximumRedeliveries(2) // in total 3 times
                .redeliveryDelay(2000)
                .logStackTrace(false)
                .logRetryAttempted(true)
                .retryAttemptedLogLevel(LoggingLevel.WARN);


        rest("/api").get("/trigger/").to("direct:help");

        from("direct:help")
                .setBody(constant("Please specify an id as path parameter: 404-404-200 always-200 always-404 slow-404"));

        rest("/api").get("/trigger/{id}").to("direct:main");

        from("direct:main").routeId("main")
                .log("Starting main route...")
                .to("direct:retriable")
                .log("Main route ends.");

        from("direct:retriable").routeId("retriable")
                .errorHandler(noErrorHandler()) // mandatory to allow the retry of the full route
                .removeHeaders("CamelHttp*") // remove headers got from /api/trigger/xxxxxx
                .log("Starting route retriable ...")
                .setProperty("start").message(m -> LocalDateTime.now()) // must be evaluated at runtime
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET)) // the http method is mandatory when using camel-http
                .log("Calling the underlying WebRoute api: ${header.id}")
                .toD("http://localhost:8080/provider/${header.id}?throwExceptionOnFailure=false") // important using the toD for dynamic
                .choice()

                .when(and(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo("404"), method(this, "isBelowThreshold")))
                .log("Not found and below the threshold").throwException(new RetriableException())
                .endChoice()

                .when(not(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo("200")))
                .log("Unretriable error").throwException(new UnretriableException())
                .endChoice()

                .otherwise()
                .log("Status code: ${headers.CamelHttpResponseCode}")
                .log("Done, body is: ${body}")
                .endChoice()

                .end();
    }

    public boolean isBelowThreshold(@ExchangeProperty("start") LocalDateTime start) {
        LocalDateTime now = LocalDateTime.now();
        long between = ChronoUnit.MILLIS.between(start, now);

        log.info("Start {} and End {}", start, now);
        log.info("Time spent was: {}", between);

        return between < 1000;
    }


    public static class RetriableException extends RuntimeException {
    }

    public static class UnretriableException extends RuntimeException {
    }

}
