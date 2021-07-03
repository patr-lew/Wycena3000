package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.aop.ErrorController;
import com.lewandowski.wycena3000.entity.Part;
import com.lewandowski.wycena3000.entity.PartType;
import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.entity.User;
import com.lewandowski.wycena3000.security.CurrentUser;
import com.lewandowski.wycena3000.service.PartService;
import com.lewandowski.wycena3000.service.ProjectService;
import com.lewandowski.wycena3000.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.testSecurityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PartController.class)
class PartControllerTest {

    @MockBean
    private PartService partService;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserService userService;

    private MockMvc mockMvc;

    private final Long USER_ID = 1L;
    private final Long WRONG_PART_ID = 7L;
    private final Long PART_ID = 3L;

    private final User MOCK_USER = User.builder()
            .id(USER_ID)
            .build();

    private final Part WRONG_PART = Part.builder()
            .id(WRONG_PART_ID)
            .user(new User())
            .build();

    private final Part testPart = Part.builder()
            .name("part name")
            .id(PART_ID)
            .user(MOCK_USER)
            .build();

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
                .defaultRequest(get("/").secure(true).with(testSecurityContext()))
                .build();
    }


    @Test
    public void shouldReturnVIewWithAllParts() throws Exception {
        // given
        Set<Long> testSet = Set.of(3L);


        when(partService.getPartsByUser(any())).thenReturn(List.of(testPart));
        when(partService.getEnabledDeleteSet()).thenReturn(testSet);

        // when + then
        mockMvc
                .perform(get("/creator/parts/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("part/part_all"))
                .andExpect(model().attribute("parts", contains(testPart)))
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
        PartType testType = new PartType();
        List<PartType> partTypes = List.of(testType);

        when(partService.findById(PART_ID)).thenReturn(testPart);
        when(partService.getPartTypes()).thenReturn(partTypes);


        // when + then
        mockMvc
                .perform(get("/creator/parts/edit/" + PART_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute("part", testPart));

    }

    @Test
    public void givenRightUser_shouldDeletePart() throws Exception {
        // given
        when(partService.findById(PART_ID)).thenReturn(testPart);

        // when + then
        mockMvc
                .perform(get("/creator/parts/delete/" + PART_ID))
                .andExpect(redirectedUrl("/creator/parts/all"));
    }

    @Test
    public void givenWrongUser_shouldRespond403() throws Exception {
        // given
        when(partService.findById(WRONG_PART_ID)).thenReturn(WRONG_PART);

        // when + then
        mockMvc
                .perform(get("/creator/parts/delete/" + WRONG_PART_ID))
                .andExpect(status().is(403))
                .andExpect(view().name("error/403"));
    }

    @Test
    public void shouldReturnChangePartFormView() throws Exception {
        // given
        Project project = Project.builder()
                .id(1L)
                .user(MOCK_USER)
                .build();

        List<Part> usersParts = List.of(new Part());

        when(projectService.findById(anyLong())).thenReturn(project);
        when(partService.getPartsByUser(project.getUser())).thenReturn(usersParts);

        // when + then
        mockMvc
                .perform(get("/creator/parts/change?partId="
                        + PART_ID + "&projectId=" + project.getId()))
                .andExpect(model().attribute("project", project))
                .andExpect(model().attribute("oldPartId", PART_ID))
                .andExpect(model().attribute("parts", usersParts))
                .andExpect(view().name("part/part_change"));
    }

    // shouldRedirectToProjectDetails


}