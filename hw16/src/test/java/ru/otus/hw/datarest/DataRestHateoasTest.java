package ru.otus.hw.datarest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("HATEOAS API на основе Spring Data REST")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DataRestHateoasTest {

    @Autowired
    private MockMvc mvc;

    @DisplayName("должен возвращать ссылки на все ресурсы в корне API")
    @Test
    void shouldReturnRootLinks() throws Exception {
        mvc.perform(get("/datarest"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$._links.books.href").exists())
                .andExpect(jsonPath("$._links.authors.href").exists())
                .andExpect(jsonPath("$._links.genres.href").exists())
                .andExpect(jsonPath("$._links.comments.href").exists());
    }

    @DisplayName("должен возвращать книгу со ссылками на автора и жанры")
    @Test
    void shouldReturnBookWithLinks() throws Exception {
        mvc.perform(get("/datarest/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("BookTitle_1"))
                .andExpect(jsonPath("$._links.self.href", endsWith("/datarest/books/1")))
                .andExpect(jsonPath("$._links.author.href", endsWith("/datarest/books/1/author")))
                .andExpect(jsonPath("$._links.genres.href", endsWith("/datarest/books/1/genres")));
    }

    @DisplayName("должен возвращать автора и жанры книги по ссылкам")
    @Test
    void shouldFollowBookAssociations() throws Exception {
        mvc.perform(get("/datarest/books/1/author"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Author_1"))
                .andExpect(jsonPath("$._links.self.href", endsWith("/datarest/authors/1")));

        mvc.perform(get("/datarest/books/1/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.genres", hasSize(2)));
    }

    @DisplayName("должен возвращать страницу книг с метаданными пагинации")
    @Test
    void shouldReturnPagedBooks() throws Exception {
        mvc.perform(get("/datarest/books").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.books", hasSize(2)))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$._links.next.href").exists());
    }

    @DisplayName("должен создавать комментарий со ссылкой на книгу")
    @Test
    void shouldCreateCommentLinkedToBook() throws Exception {
        var location = mvc.perform(post("/datarest/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Nice book\",\"book\":\"/datarest/books/2\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location + "/book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("BookTitle_2"));
    }
}
