package lv.all_sins;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        // Clear log files.
        SimpleLogger.clearLogs();

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

        SimpleLogger.appLog("startUnixMillis:"+startUnixMillis);
        SimpleLogger.appLog("endUnixMillis:"+endUnixMillis);
        SimpleLogger.appLog("invokeURL:"+invokeURL);

        // Use lastId parameter to implement pagination for
        // all of the results in the range of the request.
        JSONArray resultsAsJSONObj = new JSONArray();
        // Default OpenJDK-17-JRE crashed @ 60559000th element.
        // MaxHeapSize: 4171235328 bytes (approximately 3.88 GB)

        // Based on the fields in the AggregateTrade class:
        // 4 long fields: 4 * 8 bytes = 32 bytes
        // 2 double fields: 2 * 8 bytes = 16 bytes
        // 2 boolean fields: 2 * 1 byte = 2 bytes
        // Additional overhead for object: varies by JVM implementation, but typically around 12-16 bytes

        // Total estimated size per AggregateTrade object: ~62 bytes
        // 62 bytes * 60559000 = 3'754'718'000 bytes
        // 3'754'718'000 bytes / (1024 * 1024 * 1024) bytes/GB ≈ 3.50 GB

        // Meaning I will need anywhere from 48 to 60 GB of RAM to store all of the trade data.
        // Since I do not have such monstrous memory on any of my machines,
        // TODO: I will need to figure out a way to compute 1'049'568'772 elements without loading all of them into memory.
        // Option 1: Store everything on disk and then compute from disk. (Heavy resource usage, but retains history, great for debugging.)
        // Option 2: Compute as stream, fetch one page, compute and discard. (Light resource usage, but no history.)
        // Option 3: Compute as stream, fetch one page, log stats and discard. (Middle-ground between the two.)
        ArrayList<AggregateTrade> aggTrades = new ArrayList<>();
        boolean firstRun = true;
        boolean debugSingleRun = false;
        boolean execOnce = false;
        boolean trueLastIdFound = false;
        boolean trueLastIdInitialized = false;
        long trueLastId = 0;
        int iteration = 0;
        do {
            // Add fromId parameter for subsequent requests.
            if (!firstRun && !execOnce) {
                // TODO: Fix inclusive page ranges. I think currently there is a single item of duped data.
                long lastItemId = resultsAsJSONObj.getJSONObject(resultsAsJSONObj.length() - 1).getLong("a");
                apiRequest.clearUrlParams();
                apiRequest.addUrlParam(new URLParameter("fromId", String.valueOf(lastItemId)));
                apiRequest.addUrlParam(new URLParameter("symbol", spotPairSymbol));
                apiRequest.addUrlParam(new URLParameter("limit", String.valueOf(pageMax)));
                invokeURL = apiRequest.buildURL();
                execOnce = true;
            }
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
                SimpleLogger.appLog("Response Code: " + responseCode);

                // Read the response body
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                SimpleLogger.apiLog(response.toString());
                connection.disconnect();


            } catch (Exception e) {
                e.printStackTrace();
            }

            // Start to work with the response.
            // Investigate how to bypass resulting 1000 item limit:
            // 1672531200000                           ->     1672531559974
            // Sunday, January 1, 2023 12:00:00 AM     ->     Sunday, January 1, 2023 12:05:59.974 AM
            // First and last result of the 1000 items respectively.

            resultsAsJSONObj = new JSONArray(response.toString());
            if (!trueLastIdInitialized) {
                trueLastId = resultsAsJSONObj.getJSONObject(0).getLong("l");
                trueLastIdInitialized = true;
            }
            SimpleLogger.appLog("Index:0 AggID:"+resultsAsJSONObj.getJSONObject(0).getLong("a"));
            SimpleLogger.appLog("Index:1000 AggID:"+resultsAsJSONObj.getJSONObject(resultsAsJSONObj.length() - 1).getLong("a"));
            for (int i = 0; i < resultsAsJSONObj.length(); i++) {
                aggTrades.add(AggregateTrade.fromJson(resultsAsJSONObj.getJSONObject(i)));
                if (resultsAsJSONObj.getJSONObject(i).getLong("a") == trueLastId) {
                    SimpleLogger.appLog("trueLastId found!");
                    SimpleLogger.appLog("Expected: "+trueLastId);
                    SimpleLogger.appLog("Found: "+resultsAsJSONObj.getJSONObject(i).getLong("a"));
                    trueLastIdFound = true;
                    break;
                }
            }
            SimpleLogger.appLog("aggTrades size:"+aggTrades.size());

            iteration++;

            if (firstRun) {
                firstRun = false;
            }

            // Throttle requests to user presses for debug.
            // Scanner scanner = new Scanner(System.in);
            // scanner.nextLine();
        } while (resultsAsJSONObj.length() == 1000 || !trueLastIdFound);

        SimpleLogger.appLog("Fetching of data finnished!");
        SimpleLogger.appLog("Calculating "+aggTrades.size()+" objects...");

        final DecimalFormat decimalFormat = new DecimalFormat("#.########");
        double priceSum = 0;
        long totalTrades = aggTrades.size();
        for (AggregateTrade aggTrade : aggTrades) {
            priceSum += aggTrade.getPrice();
        }

        SimpleLogger.resultLog("priceSum:"+priceSum);
        SimpleLogger.resultLog("totalTrades:"+totalTrades);
        SimpleLogger.resultLog("priceSum/totalTrades:"+priceSum / totalTrades);
        SimpleLogger.resultLog("decimalFormat8Digits@priceSum/totalTrades:"+decimalFormat.format(priceSum / totalTrades));
    }
}