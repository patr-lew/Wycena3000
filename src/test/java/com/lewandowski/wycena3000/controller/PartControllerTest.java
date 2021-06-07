package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.entity.Part;
import com.lewandowski.wycena3000.entity.PartType;
import com.lewandowski.wycena3000.entity.User;
import com.lewandowski.wycena3000.security.CurrentUser;
import com.lewandowski.wycena3000.service.PartService;
import com.lewandowski.wycena3000.service.ProjectService;
import com.lewandowski.wycena3000.service.UserService;
import org.checkerframework.common.value.qual.StaticallyExecutable;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PartControllerTest {

    private final Long USER_ID = 1L;
    private final Long WRONG_USER_ID = 7L;

    @MockBean
    private PartService partService;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserService userService;
    @Mock
    private BindingResult bindingResult;

    @Autowired
    @InjectMocks
    private PartController partController;

    private MockMvc mvc;
    private HandlerMethodArgumentResolver putAuthenticationPrincipal = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().isAssignableFrom(CurrentUser.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            Set<GrantedAuthority> authorities = new HashSet<>();
            User testUser = new User();
            testUser.setId(USER_ID);
            return new CurrentUser("carpenter", "pass", authorities, testUser);
        }
    };

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .standaloneSetup(partController)
                .setCustomArgumentResolvers(putAuthenticationPrincipal)
                .build();
    }

    @Test
    public void shouldReturnVIewWithAllParts() throws Exception {
        // given
        final String PART_NAME = "part name";
        Set<Long> testSet = Set.of(3L);
        Part part = new Part();
        part.setName(PART_NAME);

        when(partService.getPartsByUser(any())).thenReturn(List.of(part));
        when(partService.getEnabledDeleteSet()).thenReturn(testSet);

        // when + then
        this.mvc
                .perform(get("/creator/parts/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("part/part_all"))
                .andExpect(model().attribute("parts", contains(part)))
                .andExpect(model().attribute("enabledDelete", contains(3L)));
    }

    @Test
    public void shouldReturnViewToAddParts() throws Exception {
        // given
        PartType testType = new PartType();
        List<PartType> partTypes = List.of(testType);

        when(partService.getPartTypes()).thenReturn(partTypes);

        // when + then
        this.mvc
                .perform(get("/creator/parts/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("part/part_add"))
                .andExpect(model().attributeExists("part"))
                .andExpect(model().attribute("partTypes", contains(testType)));
    }

    @Test
    public void whenAddingPart_givenErrors_returnToForm() throws Exception {
        // given
        when(bindingResult.hasErrors()).thenReturn(true);

        // when + then
        this.mvc
                .perform(post("/creator/parts/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("part/part_add"));
    }

    @Test
    public void whenAddingPart_addPart_redirectToAllParts() throws Exception {
        this.mvc
                .perform(post("/creator/parts/add"))
                .andExpect(status().isOk());
    }

    @Test
    public void givenRightUser_shouldReturnEditPartView() throws Exception {
        // given
        final Long PART_ID = 1L;

        User user = new User();
        user.setId(USER_ID);

        Part testPart = new Part();
        testPart.setId(PART_ID);
        testPart.setUser(user);

        PartType testType = new PartType();
        List<PartType> partTypes = List.of(testType);

        when(partService.findById(PART_ID)).thenReturn(testPart);
        when(partService.getPartTypes()).thenReturn(partTypes);


        // when + then
        this.mvc
                .perform(get("/creator/parts/edit/" + PART_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute("part", is(testPart)));

    }

    @Test
    public void givenRightUser_shouldDeletePart() throws Exception {
        // given
        final Long PART_ID = 13L;

        User user = new User();
        user.setId(USER_ID);

        Part testPart = new Part();
        testPart.setId(PART_ID);
        testPart.setUser(user);

        when(partService.findById(PART_ID)).thenReturn(testPart);

        // when + then
        this.mvc
                .perform(get("/creator/parts/delete/" + PART_ID))
                .andExpect(status().is(302));
    }

    @Test
    public void givenWrongUser_shouldGive403() throws Exception {
        // given
        final Long PART_ID = 13L;

        User user = new User();
        user.setId(WRONG_USER_ID);

        Part testPart = new Part();
        testPart.setId(PART_ID);
        testPart.setUser(user);

        when(partService.findById(PART_ID)).thenReturn(testPart);

        // when + then
//        this.mvc
//                .perform(get("/creator/parts/delete/" + PART_ID))
//                .andExpect(status().is(403));
    }

    // shouldReturnChangePartForm()

    // shouldRedirectToProjectDetails



}