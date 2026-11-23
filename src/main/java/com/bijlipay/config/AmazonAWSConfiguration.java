package com.bijlipay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class AmazonAWSConfiguration {
    @Value("${amazon.endpoint.url}")
    private String amazonEndpointUrl;
    @Value("${amazon.access.key}")
    private String amazonAccessKey;
    @Value("${amazon.secret.key}")
    private String amazonSecretKey;

    @Bean
    public S3Client s3Client() {
        return (S3Client)((S3ClientBuilder)((S3ClientBuilder)S3Client.builder().region(Region.of("ap-south-1"))).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(this.amazonAccessKey, this.amazonSecretKey)))).build();
    }
}