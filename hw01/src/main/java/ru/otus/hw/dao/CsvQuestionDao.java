package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.CsvToBeanFilter;
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
        var loader = getClass().getClassLoader();
        try (InputStream inputStream = loader.getResourceAsStream(fileNameProvider.getTestFileName())) {
            if (inputStream != null) {
                return processQuestionsCSVFile(inputStream);
            }
            throw new QuestionReadException("can't read CSV file");
        } catch (Exception exception) {
            throw new QuestionReadException(exception.getMessage(), exception);
        }
    }

    private List<Question> processQuestionsCSVFile(InputStream inputStream) {
        final CsvToBeanFilter commentFilter = new CsvToBeanFilter() {
            @Override
            public boolean allowLine(String[] line) {
                return line == null || line.length <= 0 || line[0] == null || !line[0].trim().startsWith("#");
            }
        };

        return new CsvToBeanBuilder<QuestionDto>(new InputStreamReader(inputStream))
                .withFilter(commentFilter)
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
