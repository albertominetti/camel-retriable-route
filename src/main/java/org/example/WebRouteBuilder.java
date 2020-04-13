package org.example;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.builder.RouteBuilder;

public class WebRouteBuilder extends RouteBuilder {

    private Counter counter = new Counter();

    @Override
    public void configure() throws Exception {
        restConfiguration().component("jetty").host("localhost").port("8080");

        rest("/provider")
                .get("/always-404").to("direct:404")
                .get("/slow-404").to("direct:slow-404")
                .get("/slow-200").to("direct:slow-200")
                .get("/always-200").to("direct:200")
                .get("/404-404-200").to("direct:404-404-200");

        from("direct:404")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404));

        from("direct:slow-404")
                .delay(2000)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404));


        from("direct:200")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setBody(constant("duecento"));

        from("direct:slow-200")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setBody(constant("duuuuuuueeeeeeeceeeeeeeentoooooo"));


        from("direct:404-404-200")
                .choice()
                .when(method(counter))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                    .setBody(constant("ciao"))
                .endChoice()
                .otherwise()
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .endChoice()
        .end();
    }

    public static class Counter {
        private int counter = 0;

        @Handler
        public boolean count() {
            counter = (counter +1) % 3;
            return counter == 0;
        }

    }
}
