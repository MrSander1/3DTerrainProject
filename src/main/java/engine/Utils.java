package engine;

import java.io.IOException;
import java.nio.file.*;


public class Utils {
    // Utility class <- this was the issue
    private Utils() {

    }

    public static String readFile(String resourcePath) {
        try (var is = Utils.class.getResourceAsStream("/" + resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Could not find file: " + resourcePath);
            }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException excp) {
            throw new RuntimeException("Error reading resource [" + resourcePath + "]", excp);
        }
    }
}