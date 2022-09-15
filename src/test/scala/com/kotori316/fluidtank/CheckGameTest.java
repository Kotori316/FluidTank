package com.kotori316.fluidtank;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class CheckGameTest {
    @Test
    void checkAll() throws IOException {
        var myTestJson = getMyTestJson();
        var names = StreamSupport.stream(GsonHelper.getAsJsonArray(GsonHelper.getAsJsonObject(myTestJson, "entrypoints"),
                "fabric-gametest").spliterator(), false)
            .map(JsonElement::getAsString)
            .map(s -> s.substring(s.lastIndexOf('.') + 1))
            .collect(Collectors.toUnmodifiableSet());

        try (var testFiles = Files.walk(Path.of("src/test/scala/com/kotori316/fluidtank/gametest"))) {
            var expected = testFiles.map(Path::getFileName)
                .map(Path::toString)
                .filter(s -> s.endsWith("java"))
                .map(s -> s.substring(0, s.lastIndexOf('.')))
                .filter(s -> s.endsWith("Test")).collect(Collectors.toUnmodifiableSet());
            Assertions.assertEquals(expected, names);
        }
    }

    private static JsonObject getMyTestJson() throws IOException {
        var modJsonFiles = CheckGameTest.class.getClassLoader().getResources("fabric.mod.json");
        Iterable<URL> iterable = modJsonFiles::asIterator;
        for (URL url : iterable) {
            try (InputStream stream = url.openStream();
                 InputStreamReader reader = new InputStreamReader(stream)) {
                var json = GsonHelper.parse(reader);
                if ("fluidtank-test".equals(GsonHelper.getAsString(json, "id"))) {
                    return json;
                }
            }
        }
        throw new AssertionError("No fabric.mod.json found.");
    }
}
