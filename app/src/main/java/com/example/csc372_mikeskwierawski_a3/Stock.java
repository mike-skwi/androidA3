package com.example.csc372_mikeskwierawski_a3;

public class Stock {

    String stockSymbol;
    String companyName;
    Double price;
    Double priceChange;
    Double changePercentage;

    Boolean positive;

    public Stock( String stockSymbol, String companyName, Double price, Double priceChange, Double changePercentage){
        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
        this.price = price;
        this.priceChange = priceChange;
        this.changePercentage = changePercentage;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }


    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public double getPriceChange() {
        if (priceChange <= 0.00){
            this.positive = false;
        }
        return priceChange;
    }

    public void setPriceChange(Double priceChange) {
        this.priceChange = priceChange;
    }

    public double getChangePercentage() {
        return changePercentage;
    }

    public String getChangeString(){
        return String.valueOf(this.priceChange);
    }

    public String getPriceString(){
        return String.valueOf(this.price);
    }

    public void setChangePercentage(Double changePercentage) {
        this.changePercentage = changePercentage;
    }
}
