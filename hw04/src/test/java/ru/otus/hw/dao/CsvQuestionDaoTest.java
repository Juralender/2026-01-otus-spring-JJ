package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CsvQuestionDao")
class CsvQuestionDaoTest {

    @Test
    @DisplayName("должен загружать вопросы из существующего CSV ресурса")
    void shouldLoadQuestionsFromExistingResource() {
        TestFileNameProvider fileNameProvider = mock(TestFileNameProvider.class);
        when(fileNameProvider.getTestFileName()).thenReturn("test-questions.csv");

        CsvQuestionDao dao = new CsvQuestionDao(fileNameProvider);
        List<Question> questions = dao.findAll();

        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).text()).isEqualTo("Is there life on Mars?");
        assertThat(questions.get(0).answers()).hasSize(3);
    }

    @Test
    @DisplayName("должен выбрасывать QuestionReadException когда ресурс не существует")
    void shouldThrowExceptionWhenResourceDoesNotExist() {
        TestFileNameProvider fileNameProvider = mock(TestFileNameProvider.class);
        when(fileNameProvider.getTestFileName()).thenReturn("non-existing-resource.csv");

        CsvQuestionDao dao = new CsvQuestionDao(fileNameProvider);

        assertThatThrownBy(dao::findAll)
                .isInstanceOf(QuestionReadException.class);
    }
}
