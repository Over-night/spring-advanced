package org.example.expert.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
public class AuthUserArgumentResolverTest {
    @Autowired
    private AuthUserArgumentResolver authUserArgumentResolver;

    @Test
    void componentIsLoaded() {
        assertThat(authUserArgumentResolver).isNotNull();
    }
}
