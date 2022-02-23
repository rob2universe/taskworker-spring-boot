package io.camunda.example;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
public class Application {

  private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @ZeebeWorker(type = "${jobType:log}")
  public void handleJob(final JobClient client, final ActivatedJob job) {

    LOG.info("\n  Worker for task type {} processing job {} \n", job.getType(), job.toJson());

    LOG.info("--- Variables ---");
    for (var entry : job.getVariablesAsMap().entrySet()) LOG.info("{} : {}", entry.getKey(), entry.getValue());

    Map<String, Object> output = Map.of("worker-"+job.getType(), System.currentTimeMillis());
    LOG.info("return variables: {}", output);

    client.newCompleteCommand(job.getKey()).variables(output).send()
        .whenComplete(
            (response, exception) -> {
              if (exception == null)
                LOG.info("Successfully completed job {} with result {}", job.getElementInstanceKey(), response);
              else
                LOG.error("Failed to complete job", exception);
            }
        );
  }
}