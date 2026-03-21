package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        try {
            final InputStream inputStream = getClass().getResourceAsStream(fileNameProvider.getTestFileName());
            if (inputStream != null) {
                return processQuestionsCSVFile(inputStream);
            }
            throw new QuestionReadException("can't read CSV file");
        } catch (Exception exception) {
            throw new QuestionReadException(exception.getMessage(), exception);
        }
    }

    private List<Question> processQuestionsCSVFile(InputStream inputStream) {
        // Использовать CsvToBean
        // https://opencsv.sourceforge.net/#collection_based_bean_fields_one_to_many_mappings
        // Использовать QuestionReadException
        // Про ресурсы: https://mkyong.com/java/java-read-a-file-from-resources-folder/
        return new CsvToBeanBuilder<QuestionDto>(new InputStreamReader(inputStream))
                .withIgnoreEmptyLine(true)
                .withIgnoreLeadingWhiteSpace(true)
                .withSeparator(';')
                .withType(QuestionDto.class)
                .build()
                .parse()
                .stream()
                .map(QuestionDto::toDomainObject)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
