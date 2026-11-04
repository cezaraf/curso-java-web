package dev.cezar.javaweb.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("aws-s3")
public class AwsS3Integracao {

    public AwsS3Integracao() {
        System.out.println("AwsS3Integracao");
    }
}
