package lv.all_sins;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        String apiEndpoint = "https://api.binance.com";
        String apiTarget = "/api/v3/aggTrades";
        String spotPairSymbol = "ETHUSDT";
        int pageMax = 1000;

        String startDateString = "2023-01-01 00:00:00:000 Z";
        String endDateString = "2023-01-01 23:59:59:999 Z";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS zzz");

        OffsetDateTime parsedStartDate = OffsetDateTime.parse(startDateString, formatter.withZone(ZoneId.of("UTC")));
        OffsetDateTime parsedEndDate = OffsetDateTime.parse(endDateString, formatter.withZone(ZoneId.of("UTC")));

        long startUnixMillis = APIRequest.offsetDateTimeToUnixMillis(parsedStartDate);
        long endUnixMillis = APIRequest.offsetDateTimeToUnixMillis(parsedEndDate);

        APIRequest apiRequest = new APIRequest(apiEndpoint, apiTarget);
        apiRequest.addUrlParam(new URLParameter("symbol", spotPairSymbol));
        apiRequest.addUrlParam(new URLParameter("limit", String.valueOf(pageMax)));
        apiRequest.addUrlParam(new URLParameter("startTime", String.valueOf(startUnixMillis)));
        apiRequest.addUrlParam(new URLParameter("endTime", String.valueOf(endUnixMillis)));
        String invokeURL = apiRequest.buildURL();

        System.out.println(startUnixMillis);
        System.out.println(endUnixMillis);
        System.out.println(invokeURL);

        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(invokeURL);
            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Optional: Set request headers if needed
            // connection.setRequestProperty("Content-Type", "application/json");
            // connection.setRequestProperty("Authorization", "Bearer YOUR_ACCESS_TOKEN");
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read the response body
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            System.out.println("Response Body: " + response);
            connection.disconnect();

            Path filePath = Path.of("api.response");
            try {
                // Write the data to the file.
                Files.writeString(filePath, response, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                System.out.println("Writing response to "+filePath);
            } catch (IOException e) {
                System.err.println("Failed to write to file"+filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start to work with the response.
        // Investigate how to bypass resulting 1000 item limit:
        // 1672531200000                           ->     1672531559974
        // Sunday, January 1, 2023 12:00:00 AM     ->     Sunday, January 1, 2023 12:05:59.974 AM
        // First and last result of the 1000 items respectively.
        JSONArray trades = new JSONArray(response.toString());

    }
}