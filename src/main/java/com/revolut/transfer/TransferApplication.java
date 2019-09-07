package com.revolut.transfer;

import lombok.Getter;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

@ApplicationPath("/api/v1")
@Getter
public class TransferApplication extends Application {

    private final TransferApplicationContext applicationContext;

    public TransferApplication() {
        this.applicationContext = TransferApplicationContext.init();
    }


    @Override
    public Set<Object> getSingletons() {
        return applicationContext.getBeans();
    }
}
