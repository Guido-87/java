package com.spring.ia.dto;

import org.junit.jupiter.api.Test;

import com.spring.ia.dto.UserInput;

import static org.junit.jupiter.api.Assertions.*;

class UserInputTest {

    @Test
    void testUserInputCreation() {
        UserInput input = new UserInput("Hello", "user123");
        assertEquals("Hello", input.prompt());
        assertEquals("user123", input.userId());
    }

    @Test
    void testUserInputWithNullPrompt() {
        UserInput input = new UserInput(null, "user123");
        assertNull(input.prompt());
        assertEquals("user123", input.userId());
    }

    @Test
    void testUserInputWithNullUserId() {
        UserInput input = new UserInput("Hello", null);
        assertEquals("Hello", input.prompt());
        assertNull(input.userId());
    }

    @Test
    void testUserInputWithEmptyStrings() {
        UserInput input = new UserInput("", "");
        assertEquals("", input.prompt());
        assertEquals("", input.userId());
    }
}
