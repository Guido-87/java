package com.spring.ia.dto;

import org.junit.jupiter.api.Test;

import com.spring.ia.dto.UserInput;

import static org.junit.jupiter.api.Assertions.*;

class UserInputTest {

    @Test
    void testUserInputCreation() {
        UserInput input = new UserInput("user123", "Hello");
        assertEquals("user123", input.userId());
        assertEquals("Hello", input.prompt());
    }

    @Test
    void testUserInputWithNullPrompt() {
        UserInput input = new UserInput("user123", null);
        assertEquals("user123", input.userId());
        assertNull(input.prompt());
    }

    @Test
    void testUserInputWithNullUserId() {
        UserInput input = new UserInput(null, "Hello");
        assertNull(input.userId());
        assertEquals("Hello", input.prompt());
    }
}
