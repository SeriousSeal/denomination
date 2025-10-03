package com.example.eurodenomination.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "calculation_history")
public class CalculationHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "amount_in_cents", nullable = false)
    private long amountInCents;
    
    @ElementCollection
    @CollectionTable(name = "denomination_entries", 
                     joinColumns = @JoinColumn(name = "history_id"))
    @MapKeyColumn(name = "denomination")
    @Column(name = "count")
    private Map<String, Integer> denominations;
    
    @Column(name = "calculation_time", nullable = false)
    private LocalDateTime calculationTime;

    public CalculationHistory() {}

    public CalculationHistory(long amountInCents, Map<String, Integer> denominations) {
        this.amountInCents = amountInCents;
        this.denominations = denominations;
        this.calculationTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCalculationTime() {
        return calculationTime;
    }

    public void setCalculationTime(LocalDateTime calculationTime) {
        this.calculationTime = calculationTime;
    }
}
