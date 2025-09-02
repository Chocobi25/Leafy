package com.chocobi.leafy.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class BatchScheduler {
    private final JobLauncher jobLauncher;
    private final Job placeDataJob;

    @Scheduled(cron = "0 0 3 * * *")
    public void runPlaceDataJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(placeDataJob, jobParameters);
            log.info("Job '{}' started with JobExecutionId: {}", placeDataJob.getName(), jobExecution.getId());

        } catch (JobExecutionException e) {
            log.error("Job execution failed", e);
        }
    }
}
