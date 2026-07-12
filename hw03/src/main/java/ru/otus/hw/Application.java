package ru.otus.hw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.otus.hw.service.TestRunnerService;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        var context = SpringApplication.run(Application.class, args);
        var testRunnerService = context.getBean(TestRunnerService.class);
        testRunnerService.run();
    }
}