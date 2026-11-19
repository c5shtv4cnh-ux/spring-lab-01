package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.author.Author;
import kz.iitu.spring_lab_01.author.AuthorController;
import kz.iitu.spring_lab_01.author.AuthorNotFoundException;
import kz.iitu.spring_lab_01.author.AuthorService;
import kz.iitu.spring_lab_01.config.CustomAccessDeniedHandler;
import kz.iitu.spring_lab_01.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Lab 12, task 2: web slice. Only the MVC layer is started; AuthorService is a Mockito mock.
 * Security: our real SecurityFilterChain is imported and stays ON; requests carry a mock user
 * (@WithMockUser) and, for POST, a CSRF token — nothing is switched off.
 */
@WebMvcTest(AuthorController.class)
@Import({SecurityConfiguration.class, CustomAccessDeniedHandler.class})
class AuthorControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AuthorService service;

    @Test
    @WithMockUser(roles = "USER")
    void anExistingAuthorIsReturnedAsJson() throws Exception {
        given(service.byId(4L)).willReturn(new Author(4L, "Frank Herbert", "USA"));

        mockMvc.perform(get("/api/authors/4"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(4))
               .andExpect(jsonPath("$.name").value("Frank Herbert"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void aMissingAuthorBecomes404WithAProblemDetail() throws Exception {
        given(service.byId(7L)).willThrow(new AuthorNotFoundException(7L));

        mockMvc.perform(get("/api/authors/7"))
               .andExpect(status().isNotFound())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.detail").value("Author 7 not found"))
               .andExpect(jsonPath("$.title").value("Author not found"))
               .andExpect(jsonPath("$.authorId").value(7));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void aBlankNameIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/authors").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \",\"country\":\"USA\"}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.status").value(400));
        verify(service, never()).create(any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void anOrdinaryUserMayNotCreateAuthors() throws Exception {
        mockMvc.perform(post("/api/authors").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Somebody\"}"))
               .andExpect(status().isForbidden());
        verify(service, never()).create(any(), any());
    }
}
