package lv.all_sins;

public class URLParameter {
    private String name;
    private String value;
    private final StringBuilder stringBuilder = new StringBuilder();

    public URLParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String buildParam() {
        // Effectively clears the StringBuilder.
        stringBuilder.setLength(0);
        stringBuilder.append(name);
        stringBuilder.append("=");
        stringBuilder.append(value);
        return stringBuilder.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
