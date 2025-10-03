package com.example.eurodenomination.model;

public class AmountRequest {
    private String amount;

    public AmountRequest() {}

    public AmountRequest(String amount) {
        this.amount = amount;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
