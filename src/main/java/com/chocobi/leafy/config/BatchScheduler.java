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

import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class BatchScheduler {
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final JobLauncher jobLauncher;
    private final Job placeDataJob;

    @Scheduled(cron = "${batch.place-sync.cron:0 0 3 * * MON}", zone = "Asia/Seoul")
    public void runPlaceDataJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("syncDate", LocalDate.now(SEOUL_ZONE).toString())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(placeDataJob, jobParameters);
            log.info("Job '{}' finished. executionId={}, status={}",
                    placeDataJob.getName(), jobExecution.getId(), jobExecution.getStatus());

        } catch (JobExecutionException e) {
            log.error("Job execution failed", e);
        }
    }
}
