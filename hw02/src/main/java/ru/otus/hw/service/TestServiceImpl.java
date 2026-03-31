package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question: questions) {
            var answersCount = question.answers().size() - 1;
            ioService.readIntForRangeWithPrompt(
                    0,
                    answersCount,
                    TestServiceImpl.formatQuestion(question),
                    String.format("Accept only from 0 to %d", answersCount)
            );
            var isAnswerValid = false; // Задать вопрос, получить ответ
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }

    private static String formatQuestion(Question question) {
        var strBuilder = new StringBuilder();
        var text = question.text();
        var answers = question.answers();
        if (text == null || text.trim().isEmpty() || answers.isEmpty()) {
            return strBuilder.toString();
        }

        strBuilder.append(text);
        strBuilder.append("\n");

        var idx = 0;
        for (var answer: answers) {
            strBuilder.append(String.format("%d) %s\n", idx++, answer.text().trim()));
        }

        return strBuilder.toString();
    }
}
