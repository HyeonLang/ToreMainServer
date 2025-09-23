package com.example.toremainserver.dto.market;

public class MarketStatsResponse {
    
    private int totalSellOrders;
    private int totalPurchaseOrders;
    private String totalVolume;
    private String averagePrice;
    
    // 기본 생성자
    public MarketStatsResponse() {}
    
    // 생성자
    public MarketStatsResponse(int totalSellOrders, int totalPurchaseOrders, 
                              String totalVolume, String averagePrice) {
        this.totalSellOrders = totalSellOrders;
        this.totalPurchaseOrders = totalPurchaseOrders;
        this.totalVolume = totalVolume;
        this.averagePrice = averagePrice;
    }
    
    // Getter와 Setter
    public int getTotalSellOrders() {
        return totalSellOrders;
    }
    
    public void setTotalSellOrders(int totalSellOrders) {
        this.totalSellOrders = totalSellOrders;
    }
    
    public int getTotalPurchaseOrders() {
        return totalPurchaseOrders;
    }
    
    public void setTotalPurchaseOrders(int totalPurchaseOrders) {
        this.totalPurchaseOrders = totalPurchaseOrders;
    }
    
    public String getTotalVolume() {
        return totalVolume;
    }
    
    public void setTotalVolume(String totalVolume) {
        this.totalVolume = totalVolume;
    }
    
    public String getAveragePrice() {
        return averagePrice;
    }
    
    public void setAveragePrice(String averagePrice) {
        this.averagePrice = averagePrice;
    }
}
