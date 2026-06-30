package com.chocobi.leafy.config;

import com.chocobi.leafy.place.batch.ExternalPlaceSyncTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;


@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ExternalPlaceSyncTasklet externalPlaceSyncTasklet;

    @Bean(name = "externalPlaceSyncStep")
    public Step externalPlaceSyncStep() {
        DefaultTransactionAttribute transactionAttribute = new DefaultTransactionAttribute();
        transactionAttribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);

        return new StepBuilder("externalPlaceSyncStep", jobRepository)
                .tasklet(externalPlaceSyncTasklet, transactionManager)
                .transactionAttribute(transactionAttribute)
                .build();
    }

    @Bean
    public Job placeDataJob(@Qualifier("externalPlaceSyncStep") Step externalPlaceSyncStep) {
        return new JobBuilder("placeDataJob", jobRepository)
                .start(externalPlaceSyncStep)
                .build();
    }
}
