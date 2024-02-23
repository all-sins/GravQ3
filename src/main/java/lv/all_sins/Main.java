package lv.all_sins;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // Clear log files and setup log directory if enabled.
        SimpleLogger.appLog("Cleaning up...");
        SimpleLogger.clearLogs();
        SimpleLogger.initIndividualApiLogsDir();
        SimpleLogger.clearIndividualApiLogsDir();
        SimpleLogger.appLog("Done.");

        String apiEndpoint = "https://api.binance.com";
        String apiTarget = "/api/v3/aggTrades";
        String spotPairSymbol = "ETHUSDT";
        int pageMax = 1000;

        String startDateString = "2023-01-01 00:00:00:000 Z";
        String endDateString = "2023-01-01 23:59:59:999 Z";

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS zzz");
        final Runtime runtime = Runtime.getRuntime();

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
        boolean trueLastTimestampFound = false;
        long preventInclusiveRangeDupe = 0L;
        int iteration = 0;
        do {
            // Add fromId parameter for subsequent requests.
            if (!firstRun) {
                // TODO: Fix inclusive page ranges. I think currently there is a single item of duped data.
                apiRequest.clearUrlParams();
                apiRequest.addUrlParam(new URLParameter("fromId", String.valueOf(preventInclusiveRangeDupe)));
                apiRequest.addUrlParam(new URLParameter("symbol", spotPairSymbol));
                apiRequest.addUrlParam(new URLParameter("limit", String.valueOf(pageMax)));
                invokeURL = apiRequest.buildURL();
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
            SimpleLogger.appLog("Index:0 AggID:"+resultsAsJSONObj.getJSONObject(0).getLong("a"));
            SimpleLogger.appLog("Index:1000 AggID:"+resultsAsJSONObj.getJSONObject(resultsAsJSONObj.length() - 1).getLong("a"));
            long currentTimestamp = 0L;
            for (int i = 0; i < resultsAsJSONObj.length(); i++) {
                JSONObject currentJsonObj = resultsAsJSONObj.getJSONObject(i);
                currentTimestamp = currentJsonObj.getLong("T");
                if (currentTimestamp > endUnixMillis) {
                    SimpleLogger.appLog("trueLastTimestamp found!");
                    SimpleLogger.appLog("Expected: "+endUnixMillis);
                    SimpleLogger.appLog("Found: "+currentTimestamp);
                    trueLastTimestampFound = true;
                    break;
                }
                AggregateTrade currentAgg = AggregateTrade.fromJson(currentJsonObj);
                aggTrades.add(currentAgg);
            }
            AggregateTrade lastAggTrade = aggTrades.get(aggTrades.size() - 1);
            long lastId = lastAggTrade.getTradeId();
            preventInclusiveRangeDupe = lastId + 1;

            SimpleLogger.appLog("aggTrades size:"+aggTrades.size());
            long lastTimestampAdded = lastAggTrade.getTimestamp();
            OffsetDateTime lastTimestampAddedHumanReadable = APIRequest.unixMillisToOffsetDateTime(lastTimestampAdded);

            // Cool diagnostics.
            long mbUsed = ( (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) );
            System.out.println(lastTimestampAddedHumanReadable+" "+currentTimestamp+" "+aggTrades.size()+" "+mbUsed+"MB");

            iteration++;

            if (firstRun) {
                firstRun = false;
            }

            // Throttle requests to user presses for debug.
            // Scanner scanner = new Scanner(System.in);
            // scanner.nextLine();
        } while (!trueLastTimestampFound);

        SimpleLogger.appLog("Fetching of data finnished!");
        SimpleLogger.appLog("Calculating "+aggTrades.size()+" objects...");

        final DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        final DecimalFormat decimalFormat = new DecimalFormat("#.########", symbols);
        decimalFormat.setRoundingMode(RoundingMode.DOWN); // Disables rounding and just truncates instead.

        // Weighted Average Price = (Every Price * Every Quantity) / Total Quantity
        // Weighted Average Price = (totalTradeValue) / totalQuantity
        double totalTradeValue = 0;
        double totalQuantity = 0;
        for (AggregateTrade aggTrade : aggTrades) {
            totalTradeValue += aggTrade.getPrice() * aggTrade.getQuantity();
            totalQuantity += aggTrade.getQuantity();
        }
        double preformatResult = totalTradeValue / totalQuantity;
        String resultEightDigitsFloatPrecision = decimalFormat.format(preformatResult);

        SimpleLogger.resultLog("totalTradeValue:"+totalTradeValue);
        SimpleLogger.resultLog("totalQuantity:"+totalQuantity);
        SimpleLogger.resultLog("totalTradeValue/totalQuantity:"+totalTradeValue / totalQuantity);
        SimpleLogger.resultLog("decimalFormat8Digits@priceSum/totalTrades:"+ resultEightDigitsFloatPrecision);

        String decimalPointSuffix = resultEightDigitsFloatPrecision.substring(resultEightDigitsFloatPrecision.indexOf(".") + 1);
        // (hint: sum of first 8 digits after comma should be 37)
        String conditionalMessage;
        long castComputeResult = castComputeDigitSum(Long.parseLong(decimalPointSuffix));
        if (castComputeResult == 37) {
            conditionalMessage = "ValidationPassed";
        } else {
            conditionalMessage = "ValidationFailed";
        }
        SimpleLogger.resultLog(conditionalMessage);
        SimpleLogger.resultLog(decimalPointSuffix);
        SimpleLogger.resultLog("castComputeResult:"+castComputeResult);
        SimpleLogger.resultLog("expected:37");
    }

    public static long castComputeDigitSum(long number) {
        long tmpSum = 0;
        char[] chars = String.valueOf(number).toCharArray();
        for (char eachChar : chars) {
            tmpSum += (int) eachChar - '0';
        }
        return tmpSum;
    }
}