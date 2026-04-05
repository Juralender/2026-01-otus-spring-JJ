package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@DisplayName("TestServiceImpl")
class TestServiceImplTest {

    private IOService ioService;

    private QuestionDao questionDao;

    private TestServiceImpl testService;

    private Student student;

    @BeforeEach
    void setUp() {
        ioService = mock(IOService.class);
        questionDao = mock(QuestionDao.class);
        testService = new TestServiceImpl(ioService, questionDao);
        student = new Student("John", "Doe");
    }

    @Test
    @DisplayName("должен возвращать TestResult с корректным студентом")
    void shouldReturnTestResultWithCorrectStudent() {
        when(questionDao.findAll()).thenReturn(Collections.emptyList());

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getStudent()).isEqualTo(student);
    }

    @Test
    @DisplayName("должен обработать все вопросы из dao")
    void shouldProcessAllQuestions() {
        var questions = List.of(
                new Question("Q1?", List.of(new Answer("A1", true), new Answer("A2", false))),
                new Question("Q2?", List.of(new Answer("A1", false), new Answer("A2", true)))
        );
        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString())).thenReturn(0);

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getAnsweredQuestions()).hasSize(2);
        verify(ioService, times(2)).readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("должен возвращать пустой результат когда нет вопросов")
    void shouldReturnEmptyResultWhenNoQuestions() {
        when(questionDao.findAll()).thenReturn(Collections.emptyList());

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getAnsweredQuestions()).isEmpty();
        assertThat(result.getRightAnswersCount()).isZero();
    }

    @Test
    @DisplayName("должен засчитать правильный ответ когда выбран isCorrect=true")
    void shouldCountCorrectAnswerWhenIsCorrectIsTrue() {
        var question = new Question("Q1?", List.of(
                new Answer("Wrong", false),
                new Answer("Right", true)
        ));
        when(questionDao.findAll()).thenReturn(List.of(question));
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1); // выбираем ответ с isCorrect=true

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getRightAnswersCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("должен не засчитать ответ когда выбран isCorrect=false")
    void shouldNotCountAnswerWhenIsCorrectIsFalse() {
        var question = new Question("Q1?", List.of(
                new Answer("Wrong", false),
                new Answer("Right", true)
        ));
        when(questionDao.findAll()).thenReturn(List.of(question));
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(0); // выбираем ответ с isCorrect=false

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getRightAnswersCount()).isZero();
    }

    @Test
    @DisplayName("должен корректно считать правильные ответы среди нескольких вопросов")
    void shouldCountCorrectAnswersAmongMultipleQuestions() {
        var q1 = new Question("Q1?", List.of(new Answer("Right", true), new Answer("Wrong", false)));
        var q2 = new Question("Q2?", List.of(new Answer("Wrong", false), new Answer("Right", true)));
        var q3 = new Question("Q3?", List.of(new Answer("Wrong", false), new Answer("Also wrong", false)));
        when(questionDao.findAll()).thenReturn(List.of(q1, q2, q3));
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(0)  // q1: выбираем index 0 → isCorrect=true
                .thenReturn(1)  // q2: выбираем index 1 → isCorrect=true
                .thenReturn(0); // q3: выбираем index 0 → isCorrect=false

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getRightAnswersCount()).isEqualTo(2);
        assertThat(result.getAnsweredQuestions()).hasSize(3);
    }

    @Test
    @DisplayName("должен вернуть ноль правильных ответов когда все ответы неверные")
    void shouldReturnZeroWhenAllAnswersAreWrong() {
        var q1 = new Question("Q1?", List.of(new Answer("Wrong", false), new Answer("Also wrong", false)));
        var q2 = new Question("Q2?", List.of(new Answer("Nope", false), new Answer("Nah", false)));
        when(questionDao.findAll()).thenReturn(List.of(q1, q2));
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(0);

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getRightAnswersCount()).isZero();
        assertThat(result.getAnsweredQuestions()).hasSize(2);
    }
}
