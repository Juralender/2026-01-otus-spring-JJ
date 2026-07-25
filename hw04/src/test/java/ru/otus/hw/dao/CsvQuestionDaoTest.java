package ru.otus.hw.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CsvQuestionDao")
class CsvQuestionDaoTest {

    @Nested
    @SpringBootTest(properties = "test.file-name-by-locale-tag.en-US=test-questions.csv")
    @DisplayName("когда CSV ресурс существует")
    class WhenResourceExists {

        @Autowired
        private CsvQuestionDao dao;

        @Test
        @DisplayName("должен загружать вопросы из существующего CSV ресурса")
        void shouldLoadQuestionsFromExistingResource() {
            List<Question> questions = dao.findAll();

            assertThat(questions).hasSize(1);
            assertThat(questions.get(0).text()).isEqualTo("Is there life on Mars?");
            assertThat(questions.get(0).answers()).hasSize(3);
        }
    }

    @Nested
    @SpringBootTest(properties = "test.file-name-by-locale-tag.en-US=non-existing-resource.csv")
    @DisplayName("когда CSV ресурс не существует")
    class WhenResourceDoesNotExist {

        @Autowired
        private CsvQuestionDao dao;

        @Test
        @DisplayName("должен выбрасывать QuestionReadException когда ресурс не существует")
        void shouldThrowExceptionWhenResourceDoesNotExist() {
            assertThatThrownBy(dao::findAll)
                    .isInstanceOf(QuestionReadException.class);
        }
    }
}
