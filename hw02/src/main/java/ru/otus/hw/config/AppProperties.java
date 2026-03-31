package ru.otus.hw.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class AppProperties implements TestConfig, TestFileNameProvider {

    private int rightAnswersCountToPass;

    private String testFileName;

    public AppProperties(
            @Value("${test.right.answers.count}") int rightAnswersCountToPass,
            @Value("${test.file.name}") String testFileName
    ) {
        this.rightAnswersCountToPass = rightAnswersCountToPass;
        this.testFileName = testFileName;
    }

    @Override
    public int getRightAnswersCountToPass() {
        return rightAnswersCountToPass;
    }

    @Override
    public String getTestFileName() {
        return testFileName;
    }
}
