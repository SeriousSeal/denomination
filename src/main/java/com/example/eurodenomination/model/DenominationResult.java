package com.example.eurodenomination.model;

import java.util.Map;

public class DenominationResult {
    private long amountInCents;
    private Map<String, Integer> denominations;
    private String previousCalculation;
    private Map<String, Integer> differenceMap;

    public DenominationResult() {}

    public DenominationResult(long amountInCents, Map<String, Integer> denominations) {
        this.amountInCents = amountInCents;
        this.denominations = denominations;
    }

    public long getAmountInCents() {
        return amountInCents;
    }

    public void setAmountInCents(long amountInCents) {
        this.amountInCents = amountInCents;
    }

    public Map<String, Integer> getDenominations() {
        return denominations;
    }

    public void setDenominations(Map<String, Integer> denominations) {
        this.denominations = denominations;
    }

    public String getPreviousCalculation() {
        return previousCalculation;
    }

    public void setPreviousCalculation(String previousCalculation) {
        this.previousCalculation = previousCalculation;
    }

    public Map<String, Integer> getDifferenceMap() {
        return differenceMap;
    }

    public void setDifferenceMap(Map<String, Integer> differenceMap) {
        this.differenceMap = differenceMap;
    }
}
