package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void whenRequestRegistrationPageThenReturnRegisterViewWithEmptyUser() {
        var model = new ConcurrentModel();
        var view = userController.getRegistrationPage(model);

        assertThat(view).isEqualTo("users/register");
        assertThat(model.getAttribute("user")).isNotNull();
    }

    @Test
    void whenRegisterExistingEmailThenRedirectBackWithErrorFlash() {
        when(userService.save(any(User.class))).thenReturn(Optional.empty());

        var redirectAttribute = new RedirectAttributesModelMap();
        var view = userController.register(new User(), redirectAttribute);

        assertThat(view).isEqualTo("redirect:/users/register");
        assertThat(redirectAttribute.getFlashAttributes()
                .get("errormessage"))
                .isEqualTo("Пользователь с таким email уже существует");
        assertThat(redirectAttribute.getFlashAttributes()
                .get("successMessage")).isNull();
    }

    @Test
    void whenRegisterSuccessThenRedirectToIndexWithSuccessFlash() {
        var user = new User("name", "email", "password");
        when(userService.save(any(User.class))).thenReturn(Optional.of(user));

        var redirectAttribute = new RedirectAttributesModelMap();
        var view = userController.register(new User(), redirectAttribute);

        assertThat(view).isEqualTo("redirect:/index");
        assertThat(redirectAttribute.getFlashAttributes()
                .get("successMessage")).isEqualTo("Регистрация прошла успешно");
        assertThat(redirectAttribute.getFlashAttributes()
                .get("errormessage")).isNull();
    }

    @Test
    void whenRequestLoginPageThenReturnLoginView() {
        var view = userController.getLoginPage();

        assertThat(view).isEqualTo("users/login");
    }

    @Test
    void whenLoginFailsThenRedirectBackWithErrorFlash() {
        when(userService.findByEmailAndPassword(any(String.class), any(String.class))).thenReturn(Optional.empty());

        var redirectAttribute = new RedirectAttributesModelMap();
        var httpRequest = new MockHttpServletRequest();
        var session = httpRequest.getSession(false);
        var view = userController.loginUser(new User(), redirectAttribute, httpRequest);

        assertThat(view).isEqualTo("redirect:/users/login");
        assertThat(redirectAttribute.getFlashAttributes()
                .get("errormessage")).isEqualTo("Почта или пароль введены неверно");
        assertThat(session).isNull();
    }

    @Test
    void whenLoginSuccessThenPutUserIntoSessionAndRedirectToVacancies() {
        var user = new User("name", "email", "password");
        when(userService.findByEmailAndPassword(any(String.class), any(String.class))).thenReturn(Optional.of(user));

        var redirectAttribute = new RedirectAttributesModelMap();
        var httpRequest = new MockHttpServletRequest();
        var view = userController.loginUser(user, redirectAttribute, httpRequest);

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(Objects.requireNonNull(httpRequest.getSession()).getAttribute("user")).isSameAs(user);
    }

    @Test
    void whenLogoutThenSessionInvalidatedAndRedirectLogin() {
        var session = new MockHttpSession();
        var view = userController.logout(session);

        assertThat(view).isEqualTo("redirect:/users/login");
        assertThat(session.isInvalid()).isTrue();
    }
}