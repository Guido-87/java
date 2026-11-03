package com.spring.ia;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "codex.agent.enabled=true")
@AutoConfigureMockMvc
class AgentTaskSpringContextTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesEnabledLocalServiceStatusBeforeCodexProcessStarts() throws Exception {
        mockMvc.perform(get("/api/agent-tasks/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.connection").value("disconnected"))
                .andExpect(jsonPath("$.agents.length()").value(6));
    }
}
