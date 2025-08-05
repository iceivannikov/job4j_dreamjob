package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;

import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Sql2oUserRepositoryTest {
    private static Sql2oUserRepository sql2oUserRepository;

    private static Sql2o sql2o;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oCandidateRepositoryTest.class.getClassLoader()
                .getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        sql2o = configuration.databaseClient(datasource);

        sql2oUserRepository = new Sql2oUserRepository(sql2o);
    }

    @AfterEach
    public void clearUsers() {
        try (var connection = sql2o.open()) {
            connection.createQuery("DELETE FROM users").executeUpdate();
        }
    }

    @Test
    void whenSaveNewUserThenReturnUserWithId() {
        var user = new User("User", "test@mail.ru", "password");

        var result = sql2oUserRepository.save(user);

        assertTrue(result.isPresent());
        assertTrue(result.get().getId() > 0);
    }

    @Test
    void whenSaveTwoUsersWithSameEmailThenSecondSaveReturnsEmpty() {
        var user1 = new User("User1", "test@mail.ru", "password1");
        var user2 = new User("User2", "test@mail.ru", "password2");

        Optional<User> savedUser1 = sql2oUserRepository.save(user1);
        Optional<User> savedUser2 = sql2oUserRepository.save(user2);

        assertTrue(savedUser1.isPresent());
        assertTrue(savedUser1.get().getId() > 0);
        assertTrue(savedUser2.isEmpty());
    }

    @Test
    void whenFindByEmailAndPasswordThenReturnUser() {
        var correctEmail = "test@mail.ru";
        var correctPassword = "password";
        var user = new User("User", correctEmail, correctPassword);
        var incorrectEmail = "incorrectEmail";
        var incorrectPassword = "incorrectPassword";

        var result = sql2oUserRepository.save(user);
        assertTrue(result.isPresent());
        assertTrue(result.get().getId() > 0);
        assertEquals("test@mail.ru", result.get().getEmail());
        assertEquals("password", result.get().getPassword());

        Optional<User> emptyUser = sql2oUserRepository.findByEmailAndPassword(incorrectEmail, incorrectPassword);
        assertTrue(emptyUser.isEmpty());

        Optional<User> correctUser = sql2oUserRepository.findByEmailAndPassword(correctEmail, correctPassword);
        assertTrue(correctUser.isPresent());
    }
}