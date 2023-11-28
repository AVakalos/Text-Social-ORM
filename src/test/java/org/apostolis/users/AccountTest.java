package org.apostolis.users;


import org.apostolis.AppConfig;
import org.apostolis.TestSuite;
import org.apostolis.exception.AuthenticationException;
import org.apostolis.exception.InvalidTokenException;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.security.TokenManager;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.apostolis.users.application.ports.in.LoginCommand;
import org.apostolis.users.application.ports.in.LoginUseCase;
import org.apostolis.users.application.ports.in.RegisterCommand;
import org.apostolis.users.application.ports.in.RegisterUseCase;
import org.apostolis.users.domain.Role;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    static SessionFactory sessionFactory;
    static TokenManager tokenManager;
    static PasswordEncoder passwordEncoder;
    static LoginUseCase loginService;
    static RegisterUseCase registerService;
    private static final AppConfig appConfig = TestSuite.appConfig;

    @BeforeAll
    static void startDb(){
        TestSuite.initialDbSetup();
        tokenManager = appConfig.getTokenManager();
        passwordEncoder = appConfig.getPasswordEncoder();
        loginService = appConfig.getLoginService();
        registerService = appConfig.getRegisterService();
        sessionFactory = TestSuite.getSessionFactory();
    }

    @BeforeEach
    void setupDb(){
        try {
            sessionFactory.inTransaction(session -> {
                session.createNativeMutationQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
                String encoded_password = passwordEncoder.encodePassword("pass1234");
                String insert = "INSERT INTO users (username,password,role) VALUES('testuser1@test.gr',?,'FREE')";
                session.createNativeMutationQuery(insert)
                        .setParameter(1, encoded_password)
                        .executeUpdate();
            });
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Test
    void testSignUp(){
        RegisterCommand registerCommand = new RegisterCommand("testuser@test.gr","pass1234","FREE");
        registerService.registerUser(registerCommand);
        sessionFactory.inTransaction(session -> {
            UserEntity user = session.get(UserEntity.class,2);
            assertEquals("testuser@test.gr",user.getUsername());
        });

    }

    @Test
    void testUnsuccessfulSignUp(){
        RegisterCommand registerCommand = new RegisterCommand("testuser1@test.gr","pass1234","FREE");
        assertThrows(IllegalArgumentException.class,()->registerService.registerUser(registerCommand));
    }

    @Test
    void testLogin(){
        LoginCommand loginCommand = new LoginCommand("testuser1@test.gr","pass1234");
        assertDoesNotThrow(()->loginService.loginUser(loginCommand));
    }

    @Test
    void testUnsuccessfulLogin(){
        LoginCommand loginCommand = new LoginCommand("testuser1@test.gr","incorrect");
        assertThrows(AuthenticationException.class, ()->loginService.loginUser(loginCommand));
    }

    @Test
    void testAuth(){
        String token = tokenManager.issueToken("testuser1@test.gr", Role.valueOf("FREE"));
        assertTrue(tokenManager.validateToken(token));
    }

    @Test
    void testUnsuccessfulAuth(){
        String token = tokenManager.issueToken("testuser1@test.gr",Role.valueOf("FREE"));
        String InvalidToken = token+"sdd";
        assertThrows(InvalidTokenException.class, ()->tokenManager.validateToken(InvalidToken));
    }
}
