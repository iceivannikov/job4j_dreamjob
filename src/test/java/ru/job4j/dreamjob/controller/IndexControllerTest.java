package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import static org.assertj.core.api.Assertions.*;

class IndexControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        IndexController controller = new IndexController();

        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setViewResolvers(resolver)
                .build();
    }

    @Test
    void whenGetRootThenReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void whenGetIndexPathThenReturnIndexView() throws Exception {
        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void whenRequestIndexThenReturnIndexView() {
        var controller = new IndexController();
        var index = controller.getIndex();

        assertThat(index).isNotNull();
        assertThat(index).isEqualTo("index");
    }
}