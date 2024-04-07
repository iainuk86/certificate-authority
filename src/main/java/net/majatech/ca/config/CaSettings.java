package net.majatech.ca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ca")
public class CaSettings {

    private String secretUrl;

    private final Aws aws = new Aws();

    public static class Aws {
        private String s3Bucket;
        private String region;

        public String getS3Bucket() {
            return s3Bucket;
        }

        public void setS3Bucket(String s3Bucket) {
            this.s3Bucket = s3Bucket;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }
    }

    public String getSecretUrl() {
        return secretUrl;
    }

    public void setSecretUrl(String secretUrl) {
        this.secretUrl = secretUrl;
    }

    public Aws getAws() {
        return aws;
    }
}
