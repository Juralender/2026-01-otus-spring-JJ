package ru.otus.hw.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    @NonNull
    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        questionDao.findAll().forEach(question -> TestServiceImpl.formatQuestion(question, ioService));
    }

    private static void formatQuestion(Question question, IOService ioService) {
        var text = question.text();
        var answers = question.answers();
        if (text == null || text.trim().isEmpty() || answers.isEmpty()) {
            return;
        }

        ioService.printLine(text);
        var idx = 0;
        for(var answer: answers) {
            ioService.printFormattedLine("%d) %s", idx++, answer.text().trim());
        }
        ioService.printLine("");
    }
}
