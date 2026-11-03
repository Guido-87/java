package com.spring.ia.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Set;

/** Explicit allowlist for workspace file changes and low-risk automatic commands. */
public final class ApprovalPolicy {
    private ApprovalPolicy() {}

    private static final Set<String> SAFE_COMMAND_TYPES = Set.of("read", "list_files", "search");

    public static boolean isAutomaticallyApprovable(JsonNode request, Path workspace) {
        String method = request.path("method").asText("").toLowerCase();
        JsonNode params = request.path("params");
        if (params.hasNonNull("networkApprovalContext") || params.hasNonNull("additionalPermissions")) return false;
        if (method.contains("filechange") || method.contains("applypatch")) return safeFileChanges(params, workspace);
        if (!(method.contains("execcommand") || method.contains("commandexecution"))) return false;
        if (!params.path("reason").isMissingNode() && !params.path("reason").isNull() && !params.path("reason").asText().isBlank()) return false;
        String cwd = params.path("cwd").asText("");
        if (!insideWorkspace(cwd, workspace)) return false;
        JsonNode parsed = params.path("parsedCmd");
        if (!parsed.isArray() || parsed.isEmpty()) return false;
        for (JsonNode command : parsed) {
            if (!SAFE_COMMAND_TYPES.contains(command.path("type").asText())) return false;
            String path = command.path("path").asText("");
            if (!path.isBlank() && !insideWorkspace(path, workspace)) return false;
        }
        return true;
    }

    public static boolean isWorkspaceFileChange(JsonNode request, Path workspace) {
        return isWorkspaceFileChange(request, workspace, null);
    }

    public static boolean isWorkspaceFileChange(JsonNode request, Path workspace, JsonNode cachedItem) {
        String method = request.path("method").asText("").toLowerCase();
        if (!(method.contains("filechange") || method.contains("applypatch"))) return false;
        JsonNode params = request.path("params");
        if (params.hasNonNull("networkApprovalContext") || params.hasNonNull("additionalPermissions")) return false;
        if (params.hasNonNull("grantRoot")) return false;
        if (safeFileChanges(params, workspace)) return true;
        return safeFileChangeItem(cachedItem, workspace);
    }

    private static boolean safeFileChangeItem(JsonNode item, Path workspace) {
        JsonNode changes = item == null ? null : item.path("changes");
        if (changes == null || !changes.isArray() || changes.isEmpty()) return false;
        for (JsonNode change : changes) {
            String path = change.path("path").asText("");
            JsonNode kind = change.path("kind");
            String type = kind.isTextual() ? kind.asText() : kind.path("type").asText("");
            String diff = change.path("diff").asText(change.path("unified_diff").asText(""));
            if (path.isBlank() || !Set.of("add", "update", "delete").contains(type)
                    || diff.isBlank() || !insideWorkspace(path, workspace)) return false;
            String movePath = kind.path("move_path").asText(kind.path("movePath").asText(""));
            if (!movePath.isBlank() && !insideWorkspace(movePath, workspace)) return false;
        }
        return true;
    }

    private static boolean safeFileChanges(JsonNode params, Path workspace) {
        if (params.hasNonNull("grantRoot")) return false;
        JsonNode changes = params.path("fileChanges");
        if (!changes.isObject() || changes.isEmpty()) return false;
        var entries = changes.fields();
        while (entries.hasNext()) {
            var entry = entries.next();
            if (entry.getKey().isBlank() || !insideWorkspace(entry.getKey(), workspace)) return false;
            JsonNode change = entry.getValue();
            String type = change.path("type").asText();
            if (!("add".equals(type) || "update".equals(type) || "delete".equals(type))) return false;
            if ("add".equals(type) && change.path("content").asText().isBlank()) return false;
            if ("update".equals(type) && change.path("unified_diff").asText().isBlank()) return false;
        }
        return true;
    }

    private static boolean insideWorkspace(String candidate, Path workspace) {
        try {
            Path root = workspace.toAbsolutePath().normalize();
            Path realRoot = root.toRealPath();
            Path path = Path.of(candidate);
            if (!path.isAbsolute()) path = root.resolve(path);
            path = path.toAbsolutePath().normalize();
            if (!path.startsWith(root)) return false;

            Path current = root;
            for (Path part : root.relativize(path)) {
                current = current.resolve(part);
                if (Files.isSymbolicLink(current)) return false;
                if (Files.exists(current, LinkOption.NOFOLLOW_LINKS)
                        && !current.toRealPath().startsWith(realRoot)) return false;
            }
            return true;
        }
        catch (IOException | RuntimeException ex) { return false; }
    }
}
