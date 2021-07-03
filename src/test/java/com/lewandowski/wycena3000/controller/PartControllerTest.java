package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.aop.ErrorController;
import com.lewandowski.wycena3000.entity.Part;
import com.lewandowski.wycena3000.entity.PartType;
import com.lewandowski.wycena3000.entity.User;
import com.lewandowski.wycena3000.security.CurrentUser;
import com.lewandowski.wycena3000.service.PartService;
import com.lewandowski.wycena3000.service.ProjectService;
import com.lewandowski.wycena3000.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PartController.class)
class PartControllerTest {

    private final Long USER_ID = 1L;
    private final Long WRONG_USER_ID = 7L;

    @MockBean
    private PartService partService;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserService userService;


    private MockMvc mockMvc;

    private final HandlerMethodArgumentResolver putAuthenticationPrincipal = new HandlerMethodArgumentResolver() {
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
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PartController(partService, projectService))
                .setControllerAdvice(new ErrorController())
                .setCustomArgumentResolvers(putAuthenticationPrincipal)
                .build();
    }


    @Test
    public void shouldReturnVIewWithAllParts() throws Exception {
        // given
        final String partName = "part name";
        Set<Long> testSet = Set.of(3L);
        Part part = new Part();
        part.setName(partName);

        when(partService.getPartsByUser(any())).thenReturn(List.of(part));
        when(partService.getEnabledDeleteSet()).thenReturn(testSet);

        // when + then
        mockMvc
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
        mockMvc
                .perform(get("/creator/parts/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("part/part_add"))
                .andExpect(model().attributeExists("part"))
                .andExpect(model().attribute("partTypes", contains(testType)));
    }

    @Test
    public void whenAddingPart_givenErrors_returnToForm() throws Exception {

        // when + then
        mockMvc
                .perform(post("/creator/parts/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("part/part_add"));
    }

    @Test
    public void whenAddingPart_addPart_redirectToAllParts() throws Exception {
        mockMvc
                .perform(post("/creator/parts/add"))
                .andExpect(status().isOk());
    }

    @Test
    public void givenRightUser_shouldReturnEditPartView() throws Exception {
        // given
        final Long partId = 1L;

        User user = new User();
        user.setId(USER_ID);

        Part testPart = new Part();
        testPart.setId(partId);
        testPart.setUser(user);

        PartType testType = new PartType();
        List<PartType> partTypes = List.of(testType);

        when(partService.findById(partId)).thenReturn(testPart);
        when(partService.getPartTypes()).thenReturn(partTypes);


        // when + then
        mockMvc
                .perform(get("/creator/parts/edit/" + partId))
                .andExpect(status().isOk())
                .andExpect(model().attribute("part", testPart));

    }

    @Test
    public void givenRightUser_shouldDeletePart() throws Exception {
        // given
        final Long partId = 13L;

        User user = new User();
        user.setId(USER_ID);

        Part testPart = new Part();
        testPart.setId(partId);
        testPart.setUser(user);

        when(partService.findById(partId)).thenReturn(testPart);

        // when + then
        mockMvc
                .perform(get("/creator/parts/delete/" + partId))
                .andExpect(status().is(302));
    }

    @Test
    public void givenWrongUser_shouldRespond403() throws Exception {
        // given
        final Long partId = 13L;

        User user = new User();
        user.setId(WRONG_USER_ID);

        Part testPart = Part.builder()
                .id(partId)
                .user(user)
                .build();

        when(partService.findById(partId)).thenReturn(testPart);

        // when + then
        mockMvc
                .perform(get("/creator/parts/delete/" + partId))
                .andExpect(status().is(403));
    }

    // shouldReturnChangePartForm()

    // shouldRedirectToProjectDetails


}