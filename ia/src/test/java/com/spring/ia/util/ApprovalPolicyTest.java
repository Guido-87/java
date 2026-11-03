package com.spring.ia.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovalPolicyTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void permitsOnlyExplicitlyClassifiedLowRiskReadOnlyOperations(@TempDir Path workspace) {
        var request = mapper.createObjectNode().put("method", "execCommandApproval");
        var params = request.putObject("params").put("cwd", workspace.toString()).putNull("reason");
        params.putArray("parsedCmd").addObject().put("type", "read")
                .put("path", workspace.resolve("src/App.java").toString());

        assertThat(ApprovalPolicy.isAutomaticallyApprovable(request, workspace)).isTrue();
    }

    @Test
    void keepsCommandsAndUnclassifiedRequestsManual(@TempDir Path workspace) {
        var command = mapper.createObjectNode().put("method", "execCommandApproval");
        command.putObject("params").put("cwd", workspace.toString()).putArray("parsedCmd")
                .addObject().put("type", "unknown");
        var unclassified = mapper.createObjectNode().put("method", "execCommandApproval");
        unclassified.putObject("params").put("cwd", workspace.getParent().toString())
                .putArray("parsedCmd").addObject().put("type", "read")
                .put("path", workspace.getParent().resolve("outside/a").toString());

        assertThat(ApprovalPolicy.isAutomaticallyApprovable(command, workspace)).isFalse();
        assertThat(ApprovalPolicy.isAutomaticallyApprovable(unclassified, workspace)).isFalse();
    }

    @Test
    void permitsWorkspaceFileChangesAndRejectsOutsideOrBroadRequests(@TempDir Path workspace) {
        var update = fileChange(workspace.resolve("src/App.java").toString(), "update", "unified_diff", "change");
        var add = fileChange("src/New.java", "add", "content", "class New {}");
        var delete = fileChange(workspace.resolve("src/App.java").toString(), "delete", "content", "old");
        var outside = fileChange(workspace.getParent().resolve("outside/App.java").toString(), "update", "unified_diff", "change");
        var workspaceGrant = fileChange("src/App.java", "update", "unified_diff", "change");
        ((ObjectNode) workspaceGrant.path("params")).put("grantRoot", workspace.toString());

        assertThat(ApprovalPolicy.isAutomaticallyApprovable(update, workspace)).isTrue();
        assertThat(ApprovalPolicy.isAutomaticallyApprovable(add, workspace)).isTrue();
        assertThat(ApprovalPolicy.isAutomaticallyApprovable(delete, workspace)).isTrue();
        assertThat(ApprovalPolicy.isWorkspaceFileChange(delete, workspace)).isTrue();
        assertThat(ApprovalPolicy.isAutomaticallyApprovable(outside, workspace)).isFalse();
        assertThat(ApprovalPolicy.isWorkspaceFileChange(outside, workspace)).isFalse();
        assertThat(ApprovalPolicy.isAutomaticallyApprovable(workspaceGrant, workspace)).isFalse();
        assertThat(ApprovalPolicy.isWorkspaceFileChange(workspaceGrant, workspace)).isFalse();
    }

    @Test
    void rejectsFileChangesThatTraverseASymbolicLink(@TempDir Path temp) throws Exception {
        Path workspace = Files.createDirectory(temp.resolve("workspace"));
        Path outside = Files.createDirectory(temp.resolve("outside"));
        Path externalFile = Files.createFile(outside.resolve("secret.txt"));
        Path link = workspace.resolve("linked.txt");
        try {
            Files.createSymbolicLink(link, externalFile);
        } catch (IOException | UnsupportedOperationException | SecurityException ex) {
            Assumptions.assumeTrue(false, "Symbolic links are unavailable in this environment");
        }

        var request = fileChange(link.toString(), "update", "unified_diff", "change");
        assertThat(ApprovalPolicy.isWorkspaceFileChange(request, workspace)).isFalse();
        assertThat(ApprovalPolicy.isAutomaticallyApprovable(request, workspace)).isFalse();
    }

    private ObjectNode fileChange(String path, String type, String valueField, String value) {
        ObjectNode request = mapper.createObjectNode().put("method", "item/fileChange/requestApproval");
        request.putObject("params").putObject("fileChanges").putObject(path)
                .put("type", type).put(valueField, value);
        return request;
    }
}
