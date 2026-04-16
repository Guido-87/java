package test.java.com.spring.ia.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.spring.ia.IaApplication;
import com.spring.ia.controller.ViewController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ViewController.class)
@ContextConfiguration(classes = IaApplication.class)
class ViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ViewController viewController = new ViewController();

    @Test
    void testChatPageReturnsViewName() {
        String viewName = viewController.chatPage();
        assertEquals("chat", viewName);
    }

    @Test
    void testChatUiEndpointMapping() throws Exception {
        mockMvc.perform(get("/chat-ui"))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"));
    }

    private void assertEquals(String expected, String actual) {
        assert expected.equals(actual);
    }
}
