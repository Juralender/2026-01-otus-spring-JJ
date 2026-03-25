package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@DisplayName("TestServiceImpl")
class TestServiceImplTest {

    private final IOService ioService = mock(IOService.class);
    private final QuestionDao questionDao = mock(QuestionDao.class);
    private final TestServiceImpl testService = new TestServiceImpl(ioService, questionDao);

    @Test
    @DisplayName("should print question with numbered answers")
    void shouldPrintQuestionWithAnswers() {
        var answers = List.of(
                new Answer("Paris", true),
                new Answer("London", false)
        );
        var question = new Question("What is the capital of France?", answers);
        when(questionDao.findAll()).thenReturn(List.of(question));

        testService.executeTest();

        InOrder inOrder = inOrder(ioService);
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLine("Please answer the questions below%n");
        inOrder.verify(ioService).printLine("What is the capital of France?");
        inOrder.verify(ioService).printFormattedLine("%d) %s", 0, "Paris");
        inOrder.verify(ioService).printFormattedLine("%d) %s", 1, "London");
        inOrder.verify(ioService).printLine("");
    }

    @Test
    @DisplayName("should skip question with empty text")
    void shouldSkipQuestionWithEmptyText() {
        var answers = List.of(new Answer("Answer", true));
        var question = new Question("", answers);
        when(questionDao.findAll()).thenReturn(List.of(question));

        testService.executeTest();

        verify(ioService).printLine("");
        verify(ioService).printFormattedLine("Please answer the questions below%n");
        verify(ioService, never()).printFormattedLine(eq("%d) %s"), anyInt(), anyString());
    }

    @Test
    @DisplayName("should skip question with null text")
    void shouldSkipQuestionWithNullText() {
        var answers = List.of(new Answer("Answer", true));
        var question = new Question(null, answers);
        when(questionDao.findAll()).thenReturn(List.of(question));

        testService.executeTest();

        verify(ioService, never()).printFormattedLine(eq("%d) %s"), anyInt(), anyString());
    }

    @Test
    @DisplayName("should skip question with empty answers")
    void shouldSkipQuestionWithEmptyAnswers() {
        var question = new Question("Some question?", Collections.emptyList());
        when(questionDao.findAll()).thenReturn(List.of(question));

        testService.executeTest();

        verify(ioService, never()).printLine("Some question?");
    }

    @Test
    @DisplayName("should print multiple questions")
    void shouldPrintMultipleQuestions() {
        var q1 = new Question("Q1?", List.of(new Answer("A1", true)));
        var q2 = new Question("Q2?", List.of(new Answer("A2", false), new Answer("A3", true)));
        when(questionDao.findAll()).thenReturn(List.of(q1, q2));

        testService.executeTest();

        verify(ioService).printLine("Q1?");
        verify(ioService).printLine("Q2?");
        verify(ioService).printFormattedLine("%d) %s", 0, "A1");
        verify(ioService).printFormattedLine("%d) %s", 0, "A2");
        verify(ioService).printFormattedLine("%d) %s", 1, "A3");
    }
}
