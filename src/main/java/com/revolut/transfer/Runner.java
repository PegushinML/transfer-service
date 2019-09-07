package com.revolut.transfer;

import io.undertow.Undertow;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;

public class Runner {


    public static void main(final String[] args) {
        var server = new UndertowJaxrsServer();
        server.start(Undertow.builder().addHttpListener(8080, "localhost"));
        server.deploy(TransferApplication.class);
    }


}
