package dev.cezar.javaweb.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("azure-blob")
public class AzureBlobIntegracao {

    public AzureBlobIntegracao() {
        System.out.println("AzureBlobIntegracao");
    }
}
