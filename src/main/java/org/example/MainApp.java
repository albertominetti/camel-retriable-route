package org.example;

import org.apache.camel.main.Main;


public class MainApp {

    /*
     * test with the following urls:
     *
     * http://localhost:8080/api/trigger/always-404
     * http://localhost:8080/api/trigger/404-404-200
     * http://localhost:8080/api/trigger/always-200
     * http://localhost:8080/api/trigger/slow-200
     * http://localhost:8080/api/trigger/slow-404
     *
     */

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.addRoutesBuilder(new MainRouteBuilder());
        main.addRoutesBuilder(new WebRouteBuilder());
        main.run(args);
    }

}

