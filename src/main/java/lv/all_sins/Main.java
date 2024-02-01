package lv.all_sins;

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

        APIRequest apiRequest = new APIRequest(apiEndpoint, apiTarget);

        long startUnixMillis = apiRequest.offsetDateTimeToUnixMillis(parsedStartDate);
        long endUnixMillis = apiRequest.offsetDateTimeToUnixMillis(parsedEndDate);

        System.out.println(startUnixMillis);
        System.out.println(endUnixMillis);
    }
}