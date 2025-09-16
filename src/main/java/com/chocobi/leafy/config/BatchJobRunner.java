package com.chocobi.leafy.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobRunner implements CommandLineRunner {
    private final JobLauncher jobLauncher;
    private final Job placeDataJob;

    /**
     * 애플리케이션 시작 시 자동으로 실행되는 메서드
     *
     * @param args 커맨드 라인 인자
     * @throws Exception 실행 중 발생할 수 있는 예외
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting a one-time batch job run...");

        try {
            // Job 파라미터 생성
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            // Job 실행
            JobExecution jobExecution = jobLauncher.run(placeDataJob, jobParameters);
            log.info("Job '{}' started with JobExecutionId: {}", placeDataJob.getName(), jobExecution.getId());

        } catch (JobExecutionException e) {
            log.error("Job execution failed", e);
        }
    }
}
