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

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printLineLocalized("TestService.answer.the.questions");
        ioService.printLine("");

        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question: questions) {
            printQuestion(question);
            var answersCount = question.answers().size() - 1;
            var errorMessage = ioService.getMessage("TestService.answer.range.error", 0, answersCount);
            var answerIndex = ioService.readIntForRange(0, answersCount, errorMessage);
            var isAnswerValid = question.answers().get(answerIndex).isCorrect();
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }

    private void printQuestion(Question question) {
        ioService.printLine(question.text());
        var answers = question.answers();
        for (var i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("%d) %s", i, answers.get(i).text());
        }
    }

}
