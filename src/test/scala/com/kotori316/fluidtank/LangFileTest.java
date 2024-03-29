package com.kotori316.fluidtank;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class LangFileTest {
    static final Gson GSON = new Gson();

    @Test
    void findEN_USFile() {
        assertNotNull(getClass().getResource("/assets/fluidtank/lang/en_us.json"));
    }

    @TestFactory
    @SuppressWarnings("ConstantConditions")
    Stream<DynamicTest> containsAll() throws URISyntaxException, IOException {
        var langDir = Path.of(getClass().getResource("/assets/fluidtank/lang/en_us.json").toURI()).getParent();
        return DynamicTest.stream(Files.find(langDir, 1, (path, a) ->
                path.getFileName().toString().endsWith("json") && !path.getFileName().toString().startsWith("en_us")),
            path -> path.getFileName().toString(), this::containsAll
        );
    }

    void containsAll(Path languageFile) throws IOException {
        var keysInFile = GSON.fromJson(Files.newBufferedReader(languageFile), JsonObject.class)
            .keySet().stream().sorted().toList();
        assertIterableEquals(en_usKeys(), keysInFile, languageFile.getFileName().toString());
    }

    @SuppressWarnings("ConstantConditions")
    List<String> en_usKeys() {
        return GSON.fromJson(new InputStreamReader(getClass().getResourceAsStream("/assets/fluidtank/lang/en_us.json")), JsonObject.class)
            .keySet().stream().sorted().toList();
    }

    @TestFactory
    @SuppressWarnings("ConstantConditions")
    Stream<DynamicTest> noDuplication() throws URISyntaxException, IOException {
        var langDir = Path.of(getClass().getResource("/assets/fluidtank/lang/en_us.json").toURI()).getParent();
        return DynamicTest.stream(Files.find(langDir, 1, (path, a) ->
                path.getFileName().toString().endsWith("json")),
            path -> path.getFileName().toString(), this::noDuplication
        );
    }

    void noDuplication(Path path) throws IOException {
        var lineCount = Files.readAllLines(path).size() - 2; // Remove first and last line.
        var elementCount = GSON.fromJson(Files.newBufferedReader(path), JsonObject.class).size();
        assertEquals(elementCount, lineCount, "%s contains duplicated elements.".formatted(path.getFileName()));
    }
}
