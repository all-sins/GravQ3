package lv.all_sins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class SimpleLogger {
    // 25 MB log in around 20 seconds. Absolute insanity for my home server.
    // Leaving for diagnostic use, with the toggle being this hard-coded boolean.
    private static final boolean enableApiLog = false;
    private static String appendSystemNewlineSymbol(String msg) {
        return msg + System.lineSeparator();
    }

    public static void clearLogs() {
        try {
            Files.writeString(Path.of("app.log"), "", StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            Files.writeString(Path.of("api.log"), "", StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            Files.writeString(Path.of("result.log"), "", StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.err.println("Failed to clear logs!");
        }
    }
    public static void appLog(String msg) {
        Path filePath = Path.of("app.log");
        try {
            Files.writeString(filePath, appendSystemNewlineSymbol(msg), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Couldn't log application!");
        }
    }

    public static void apiLog(String msg) {
        // TODO: Better "Disable switch" for heavy API logging.
        if (!enableApiLog) {return;}
        Path filePath = Path.of("api.log");
        try {
            Files.writeString(filePath, appendSystemNewlineSymbol(msg), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to log API call!");
        }
    }

    public static void resultLog(String msg) {
        Path filePath = Path.of("result.log");
        try {
            Files.writeString(filePath, appendSystemNewlineSymbol(msg), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to log result!");
        }
    }
}
