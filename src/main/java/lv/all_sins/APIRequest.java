package lv.all_sins;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class APIRequest {
    private final String apiEndpoint;
    private final String apiTarget;
    public final String urlParamStartSymbol = "?";
    public final String urlParamDelimiter = "&";
    private final ArrayList<URLParameter> urlParams = new ArrayList<>();
    private final StringBuilder stringBuilder = new StringBuilder();

    public APIRequest(String apiEndpoint, String apiTarget) {
        this.apiEndpoint = apiEndpoint;
        this.apiTarget = apiTarget;
    }

    public String buildURL() {
        // Effectively clears StringBuilder.
        stringBuilder.setLength(0);
        stringBuilder.append(apiEndpoint);
        stringBuilder.append(apiTarget);
        stringBuilder.append(urlParamStartSymbol);
        for (int i = 0; i < urlParams.size(); i++) {
            stringBuilder.append(urlParams.get(i).buildParam());
            if (i != urlParams.size() - 1) {
                stringBuilder.append(urlParamDelimiter);
            }
        }
        return stringBuilder.toString();
    }

    public static OffsetDateTime unixMillisToOffsetDateTime(long unixTimeMillis) {
        Instant instant = Instant.ofEpochMilli(unixTimeMillis);
        return instant.atOffset(ZoneOffset.UTC);
    }

    public static long offsetDateTimeToUnixMillis(OffsetDateTime offsetDateTime) {
        return offsetDateTime.toInstant().toEpochMilli();
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public String getApiTarget() {
        return apiTarget;
    }

    public ArrayList<URLParameter> getUrlParams() {
        return urlParams;
    }

    public void addUrlParam(URLParameter urlParameter) {
        urlParams.add(urlParameter);
    }

    public void clearUrlParams() {
        urlParams.clear();
    }
}
