package com.maruhxn.todomon.core.global.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Configuration
public class S3Config {
    @Value("${aws.s3.endpointUrl}")
    private String s3EndpointUrl;
    @Value("${aws.credentials.accessKey}")
    private String accessKey;
    @Value("${aws.credentials.secretKey}")
    private String secretKey;
    @Value("${aws.s3.region}")
    private String region;

    @Bean
    public AmazonS3Client amazonS3Client() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        return (AmazonS3Client) AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3EndpointUrl, region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
}