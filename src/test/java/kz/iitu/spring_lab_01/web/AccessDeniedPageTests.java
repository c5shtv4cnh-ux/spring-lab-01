package kz.iitu.spring_lab_01.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Individual variant 3: the custom AccessDeniedHandler is used, the status stays 403. */
@SpringBootTest
@AutoConfigureMockMvc
class AccessDeniedPageTests {

    @Autowired MockMvc mvc;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void userOnAdminGetsOurPage() throws Exception {
        mvc.perform(get("/admin"))
           .andExpect(status().isForbidden())
           .andExpect(forwardedUrl("/access-denied"))
           .andExpect(request().attribute("deniedPath", "/admin"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void userOnUnmappedUrlGetsOurPage() throws Exception {
        mvc.perform(get("/nowhere"))
           .andExpect(status().isForbidden())
           .andExpect(forwardedUrl("/access-denied"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void userPostingABookGetsOurPage() throws Exception {
        mvc.perform(post("/books").with(csrf()).param("title", "T").param("author", "A").param("year", "2020"))
           .andExpect(status().isForbidden())
           .andExpect(forwardedUrl("/access-denied"));
    }

    @Test
    void anonymousIsStillRedirectedToLogin() throws Exception {
        mvc.perform(get("/admin"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void adminStillOpensAdmin() throws Exception {
        mvc.perform(get("/admin"))
           .andExpect(status().isOk())
           .andExpect(view().name("admin/users"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void templateRendersWhoIsDenied() throws Exception {
        mvc.perform(get("/access-denied").requestAttr("deniedPath", "/admin").requestAttr("deniedMethod", "GET"))
           .andExpect(view().name("error/403"))
           .andExpect(content().string(org.hamcrest.Matchers.containsString("your roles do not allow this request")));
    }
}
