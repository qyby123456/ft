package cn.farmingtales.sdmshopaudit;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class TrustedCommands {
    private final Path file;
    private final Set<String> original;
    private final Map<String, JsonObject> approved = new LinkedHashMap<>();

    public TrustedCommands(Path file, Set<String> original) throws Exception {
        this.file = file;
        this.original = Set.copyOf(original);
        if (Files.exists(file)) {
            JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.get("schemaVersion").getAsInt() != 1 || root.getAsJsonArray("commands").size() > 1024) {
                throw new IllegalArgumentException("Invalid trusted-command file");
            }
            for (var element : root.getAsJsonArray("commands")) {
                JsonObject definition = element.getAsJsonObject();
                String command = definition.get("command").getAsString();
                validate(command);
                boolean elevated = definition.get("elevatePerms").getAsBoolean();
                boolean silent = definition.get("silent").getAsBoolean();
                approved.put(SecurityRules.commandFingerprint(command, elevated, silent), definition.deepCopy());
            }
        }
    }

    public boolean contains(String fingerprint) {
        return original.contains(fingerprint) || approved.containsKey(fingerprint);
    }

    public String approve(String command, boolean elevated, boolean silent) throws Exception {
        validate(command);
        String fingerprint = SecurityRules.commandFingerprint(command, elevated, silent);
        JsonObject definition = new JsonObject();
        definition.addProperty("command", command);
        definition.addProperty("elevatePerms", elevated);
        definition.addProperty("silent", silent);
        Map<String, JsonObject> updated = new LinkedHashMap<>(approved);
        updated.put(fingerprint, definition);
        if (updated.size() > 1024) {
            throw new IllegalArgumentException("Too many approved commands");
        }
        save(updated);
        approved.clear();
        approved.putAll(updated);
        return fingerprint;
    }

    public boolean revoke(String fingerprint) throws Exception {
        if (!approved.containsKey(fingerprint)) {
            return false;
        }
        Map<String, JsonObject> updated = new LinkedHashMap<>(approved);
        updated.remove(fingerprint);
        save(updated);
        approved.clear();
        approved.putAll(updated);
        return true;
    }

    private void save(Map<String, JsonObject> definitions) throws Exception {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", 1);
        JsonArray commands = new JsonArray();
        definitions.values().forEach(commands::add);
        root.add("commands", commands);
        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temporary, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(root), StandardCharsets.UTF_8);
        Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void validate(String command) {
        if (command.isBlank() || command.length() > 4096 || command.indexOf('\n') >= 0
                || command.indexOf('\r') >= 0 || command.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("INVALID_COMMAND_TEMPLATE");
        }
    }
}
