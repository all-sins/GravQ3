package lv.all_sins;

import org.json.JSONObject;

public class AggregateTrade {
    /*
    [
      {
        "a": 26129,         // Aggregate tradeId
        "p": "0.01633102",  // Price
        "q": "4.70443515",  // Quantity
        "f": 27781,         // First tradeId
        "l": 27781,         // Last tradeId
        "T": 1498793709153, // Timestamp
        "m": true,          // Was the buyer the maker?
        "M": true           // Was the trade the best price match?
      }
    ]
    */
    private long tradeId;
    private double price; // Float has only 6 decimal digit precision.
    private double quantity; // Double has 15, which avoids data loss and enables verification of result.
    private long firstTradeId;
    private long lastTradeId; // Pagination QOL?
    private long timestamp;
    private boolean buyerWasMaker;
    private boolean tradeWasBestPriceMatch;

    private AggregateTrade() {
    }

    public static AggregateTrade fromJson(JSONObject jsonObject) {
        AggregateTrade aggTrade = new AggregateTrade();
        // "a": 26129,         // Aggregate tradeId
        aggTrade.setTradeId(jsonObject.getLong("a"));
        // "p": "0.01633102",  // Price
        aggTrade.setPrice(jsonObject.getDouble("p"));
        // "q": "4.70443515",  // Quantity
        aggTrade.setQuantity(jsonObject.getDouble("q"));
        // "f": 27781,         // First tradeId
        aggTrade.setFirstTradeId(jsonObject.getLong("f"));
        // "l": 27781,         // Last tradeId
        aggTrade.setLastTradeId(jsonObject.getLong("l"));
        // "T": 1498793709153, // Timestamp
        aggTrade.setTimestamp(jsonObject.getLong("T"));
        // "m": true,          // Was the buyer the maker?
        aggTrade.setBuyerWasMaker(jsonObject.getBoolean("m"));
        // "M": true           // Was the trade the best price match?
        aggTrade.setTradeWasBestPriceMatch(jsonObject.getBoolean("M"));
        return aggTrade;
    }

    public long getTradeId() {
        return tradeId;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public long getFirstTradeId() {
        return firstTradeId;
    }

    public void setFirstTradeId(long firstTradeId) {
        this.firstTradeId = firstTradeId;
    }

    public long getLastTradeId() {
        return lastTradeId;
    }

    public void setLastTradeId(long lastTradeId) {
        this.lastTradeId = lastTradeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isBuyerWasMaker() {
        return buyerWasMaker;
    }

    public void setBuyerWasMaker(boolean buyerWasMaker) {
        this.buyerWasMaker = buyerWasMaker;
    }

    public boolean isTradeWasBestPriceMatch() {
        return tradeWasBestPriceMatch;
    }

    public void setTradeWasBestPriceMatch(boolean tradeWasBestPriceMatch) {
        this.tradeWasBestPriceMatch = tradeWasBestPriceMatch;
    }
}
