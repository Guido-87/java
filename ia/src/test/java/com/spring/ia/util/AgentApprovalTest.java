package com.spring.ia.util;

import com.spring.ia.dto.AgentApproval;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentApprovalTest {
    @Test void requiresCommandAndAbsoluteWorkingDirectoryBeforeAcceptance() {
        assertThat(new AgentApproval("1", "command", "Review", "").canAccept()).isFalse();
        assertThat(new AgentApproval("1", "command", "Review", "[\"git\",\"status\"]").canAccept()).isFalse();
        assertThat(new AgentApproval("1", "command", "Revisar", "[\"git\",\"status\"]\nDirectorio de trabajo: C:/repo").canAccept()).isTrue();
    }

    @Test void requiresConcreteFilePathsAndChangeContent() {
        assertThat(new AgentApproval("1", "file_change", "Review", "{}").canAccept()).isFalse();
        assertThat(new AgentApproval("1", "file_change", "Review", "{\"src/A.java\":{\"type\":\"update\",\"unified_diff\":\"\"}}").canAccept()).isFalse();
        assertThat(new AgentApproval("1", "file_change", "Review", "{\"src/A.java\":{\"type\":\"update\",\"unified_diff\":\"@@ -1 +1 @@\"}}").canAccept()).isTrue();
    }
}
