package com.spring.ia.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;

public record AgentApproval(String id, String kind, String summary, String details, boolean canAccept) {
    private static final ObjectMapper JSON = new ObjectMapper();

    public AgentApproval(String id, String kind, String summary, String details) {
        this(id, kind, summary, details, isReviewable(kind, details));
    }

    static boolean isReviewable(String kind, String details) {
        if (details == null || details.isBlank()) return false;
        try {
            if ("command".equals(kind)) {
                int cwdAt = details.lastIndexOf("\nDirectorio de trabajo: ");
                if (cwdAt <= 0) return false;
                String rawCommand = details.substring(0, cwdAt).trim();
                String cwdValue = details.substring(cwdAt + "\nDirectorio de trabajo: ".length()).trim().split("\\R", 2)[0];
                Path cwd = Path.of(cwdValue);
                boolean commandIsComplete = false;
                if (rawCommand.startsWith("Acciones propuestas: ")) {
                    JsonNode actions = JSON.readTree(rawCommand.substring("Acciones propuestas: ".length()));
                    commandIsComplete = actions.isArray() && !actions.isEmpty();
                    if (commandIsComplete) for (JsonNode action : actions) {
                        String type = action.path("type").asText();
                        commandIsComplete &= SetOfChanges.COMMAND_ACTION_TYPES.contains(type)
                                && !action.path("command").asText().isBlank();
                    }
                } else {
                    try {
                        JsonNode command = JSON.readTree(rawCommand);
                        commandIsComplete = command.isArray() && !command.isEmpty() && command.get(0).isTextual()
                                && !command.get(0).asText().isBlank();
                        if (commandIsComplete) for (JsonNode argument : command) commandIsComplete &= argument.isTextual();
                    } catch (Exception ignored) { commandIsComplete = !rawCommand.isBlank(); }
                }
                return commandIsComplete && cwd.isAbsolute();
            }
            if ("file_change".equals(kind)) {
                JsonNode changes = JSON.readTree(details);
                if (changes.isArray()) {
                    if (changes.isEmpty()) return false;
                    for (JsonNode change : changes) {
                        String path = change.path("path").asText();
                        JsonNode kindNode = change.path("kind");
                        String type = kindNode.isTextual() ? kindNode.asText() : kindNode.path("type").asText();
                        String diff = change.path("diff").asText(change.path("unified_diff").asText());
                        if (path.isBlank() || !SetOfChanges.TYPES.contains(type) || diff.isBlank()) return false;
                    }
                    return true;
                }
                if (!changes.isObject() || changes.isEmpty()) return false;
                var fields = changes.fields();
                while (fields.hasNext()) {
                    var entry = fields.next();
                    JsonNode change = entry.getValue();
                    String type = change.path("type").asText();
                    if (entry.getKey().isBlank() || !SetOfChanges.TYPES.contains(type)) return false;
                    if ("update".equals(type) && change.path("unified_diff").asText().isBlank()) return false;
                    if (("add".equals(type) || "delete".equals(type)) && change.path("content").asText().isBlank()) return false;
                }
                return true;
            }
        } catch (Exception ignored) { return false; }
        return false;
    }

    private static final class SetOfChanges {
        private static final java.util.Set<String> TYPES = java.util.Set.of("add", "update", "delete");
        private static final java.util.Set<String> COMMAND_ACTION_TYPES = java.util.Set.of("read", "listFiles", "search", "unknown");
    }
}
