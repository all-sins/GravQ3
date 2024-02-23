package lv.all_sins;

import java.io.IOException;
import java.nio.file.*;

public class SimpleLogger {
    // 25 MB log in around 20 seconds. Absolute insanity for my home server.
    // Leaving for diagnostic use, with the toggle being this hard-coded boolean.
    private static final boolean enableApiLog = false;
    private static final boolean enableIndividualApiLogs = true;
    private static long individualApiLogCount = 0;
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

    public static void clearIndividualApiLogsDir() {
        String directoryPath = "./apiLogs";
        Path directory = Paths.get(directoryPath);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            if (!directoryStream.iterator().hasNext()) {
                appLog("No logs to be cleared in "+directoryPath+", skipping!");
                return;
            }
            for (Path path : directoryStream) {
                try {
                    Files.delete(path);
                    appLog("Deleting: " + path);
                } catch (IOException e) {
                    System.err.println("Failed to delete file: " + path + ", " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to list files in directory: " + directory + ", " + e.getMessage());
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
        if (!enableIndividualApiLogs) {
            Path filePath = Path.of("api.log");
            try {
                Files.writeString(filePath, appendSystemNewlineSymbol(msg), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Failed to log API call!");
            }
        } else {
            individualApiLog(msg);
        }
    }

    public static void initIndividualApiLogsDir() {
        String directoryPath = "./apiLogs";
        Path directory = Paths.get(directoryPath);
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
                appLog("Directory created successfully.");
            } catch (IOException e) {
                System.err.println("Failed to create directory: " + e.getMessage());
            }
        }
    }

    public static void individualApiLog(String msg) {
        Path filePath = Path.of("./apiLogs/"+individualApiLogCount);
        try {
            Files.writeString(filePath, appendSystemNewlineSymbol(msg), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to log result!");
        }
        individualApiLogCount++;
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
