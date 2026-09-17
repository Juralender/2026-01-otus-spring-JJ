package ru.otus.hw.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("должен открывать страницу аутентифицированному пользователю")
    @WithMockUser
    @Test
    void shouldAllowAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk());
    }

    @DisplayName("должен перенаправлять неаутентифицированного пользователя на страницу входа")
    @Test
    void shouldRedirectUnauthenticatedUserToLogin() throws Exception {
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @DisplayName("страница входа должна быть доступна без аутентификации")
    @Test
    void shouldAllowAnonymousAccessToLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @DisplayName("должен запрещать пользователю без роли ADMIN создание автора")
    @WithMockUser(roles = "USER")
    @Test
    void shouldForbidUserFromCreatingAuthor() throws Exception {
        mockMvc.perform(post("/api/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New Author\"}"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должен разрешать пользователю с ролью ADMIN создание автора")
    @WithMockUser(roles = "ADMIN")
    @Test
    void shouldAllowAdminToCreateAuthor() throws Exception {
        mockMvc.perform(post("/api/authors")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"New Author\"}"))
                .andExpect(status().isCreated());
    }

    @DisplayName("должен запрещать пользователю без роли ADMIN обновление автора")
    @WithMockUser(roles = "USER")
    @Test
    void shouldForbidUserFromUpdatingAuthor() throws Exception {
        mockMvc.perform(put("/api/authors/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Updated Author\"}"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должен разрешать пользователю с ролью ADMIN обновление автора")
    @WithMockUser(roles = "ADMIN")
    @Test
    void shouldAllowAdminToUpdateAuthor() throws Exception {
        mockMvc.perform(put("/api/authors/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Updated Author\"}"))
                .andExpect(status().isOk());
    }

    @DisplayName("должен запрещать пользователю без роли ADMIN создание жанра")
    @WithMockUser(roles = "USER")
    @Test
    void shouldForbidUserFromCreatingGenre() throws Exception {
        mockMvc.perform(post("/api/genres")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NewGenre\"}"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должен разрешать пользователю с ролью ADMIN создание жанра")
    @WithMockUser(roles = "ADMIN")
    @Test
    void shouldAllowAdminToCreateGenre() throws Exception {
        mockMvc.perform(post("/api/genres")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NewGenre\"}"))
                .andExpect(status().isCreated());
    }

    @DisplayName("должен запрещать пользователю без роли ADMIN обновление жанра")
    @WithMockUser(roles = "USER")
    @Test
    void shouldForbidUserFromUpdatingGenre() throws Exception {
        mockMvc.perform(put("/api/genres/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"UpdGenre\"}"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должен разрешать пользователю с ролью ADMIN обновление жанра")
    @WithMockUser(roles = "ADMIN")
    @Test
    void shouldAllowAdminToUpdateGenre() throws Exception {
        mockMvc.perform(put("/api/genres/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"UpdGenre\"}"))
                .andExpect(status().isOk());
    }

    @DisplayName("должен запрещать пользователю без роли ADMIN создание книги")
    @WithMockUser(roles = "USER")
    @Test
    void shouldForbidUserFromCreatingBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Book\",\"authorId\":1,\"genreIds\":[1]}"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должен разрешать пользователю с ролью ADMIN создание книги")
    @WithMockUser(roles = "ADMIN")
    @Test
    void shouldAllowAdminToCreateBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Book\",\"authorId\":1,\"genreIds\":[1]}"))
                .andExpect(status().isCreated());
    }

    @DisplayName("должен запрещать пользователю без роли ADMIN обновление книги")
    @WithMockUser(roles = "USER")
    @Test
    void shouldForbidUserFromUpdatingBook() throws Exception {
        mockMvc.perform(put("/api/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Book\",\"authorId\":1,\"genreIds\":[1]}"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должен разрешать пользователю с ролью ADMIN обновление книги")
    @WithMockUser(roles = "ADMIN")
    @Test
    void shouldAllowAdminToUpdateBook() throws Exception {
        mockMvc.perform(put("/api/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Book\",\"authorId\":1,\"genreIds\":[1]}"))
                .andExpect(status().isOk());
    }

    @DisplayName("должен запрещать пользователю без роли ADMIN удаление книги")
    @WithMockUser(roles = "USER")
    @Test
    void shouldForbidUserFromDeletingBook() throws Exception {
        mockMvc.perform(delete("/api/books/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должен разрешать пользователю с ролью ADMIN удаление книги")
    @WithMockUser(roles = "ADMIN")
    @Test
    void shouldAllowAdminToDeleteBook() throws Exception {
        var createResult = mockMvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Book To Delete\",\"authorId\":1,\"genreIds\":[1]}"))
                .andExpect(status().isCreated())
                .andReturn();
        long bookId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/books/" + bookId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @DisplayName("должен разрешать любому аутентифицированному пользователю читать каталог и комментарии")
    @WithMockUser(roles = "USER")
    @Test
    void shouldAllowAuthenticatedUserToReadCatalogAndComments() throws Exception {
        mockMvc.perform(get("/api/books")).andExpect(status().isOk());
        mockMvc.perform(get("/api/genres")).andExpect(status().isOk());
        mockMvc.perform(get("/api/books/1/comments")).andExpect(status().isOk());
    }

    @DisplayName("должен отклонять анонимный запрос на добавление комментария")
    @Test
    void shouldRejectAnonymousRequestToCreateComment() throws Exception {
        mockMvc.perform(post("/api/books/1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Anonymous comment\"}"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @DisplayName("должен разрешать любому аутентифицированному пользователю добавлять комментарий к книге")
    @WithMockUser(roles = "USER")
    @Test
    void shouldAllowUserToCreateBookComment() throws Exception {
        mockMvc.perform(post("/api/books/1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Nice book\"}"))
                .andExpect(status().isCreated());
    }

    @DisplayName("должен разрешать редактирование и удаление комментария только владельцу или администратору")
    @Test
    void shouldEnforceOwnershipBasedAuthorizationForComments() throws Exception {
        var createResult = mockMvc.perform(post("/api/books/1/comments")
                        .with(user("user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Owner comment\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long commentId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/books/1/comments/" + commentId)
                        .with(user("admin").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Hijacked\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/books/1/comments/" + commentId)
                        .with(user("user").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Edited by owner\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Edited by owner"));

        mockMvc.perform(delete("/api/books/1/comments/" + commentId)
                        .with(user("admin").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/books/1/comments/" + commentId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
