package com.example.eurodenomination.service;

import com.example.eurodenomination.model.CalculationHistory;
import com.example.eurodenomination.model.DenominationResult;
import com.example.eurodenomination.repository.CalculationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DenominationService {

    @Autowired
    private CalculationHistoryRepository calculationHistoryRepository;

    // Euro-Denominationen in Cent (von größter zu kleinster)
    private static final int[] DENOMINATIONS = {
        20000,
        10000,
        5000, 
        2000, 
        1000, 
        500, 
        200, 
        100, 
        50,   
        20,
        10,
        5,
        2, 
        1 
    };

    public DenominationResult calculateDenomination(long amountInCents) {
        return calculateDenomination(amountInCents, true);
    }

    public DenominationResult calculateDenomination(long amountInCents, boolean calculateDifference) {


        Map<String, Integer> result = new LinkedHashMap<>();
        long remainingAmount = amountInCents;

        for (int denomination : DENOMINATIONS) {
            int count = (int) (remainingAmount / denomination);
            if (count > 0) {
                result.put(String.valueOf(denomination), count);
                remainingAmount -= count * denomination;
            }
        }

        DenominationResult denominationResult = new DenominationResult(amountInCents, result);
        
        // Speichere die Berechnung in der Historie
        saveCalculation(amountInCents, result);
        
        // Vergleiche mit vorheriger Berechnung nur wenn gewünscht
        if (calculateDifference) {
            addComparisonWithPrevious(denominationResult);
        } else {
            denominationResult.setPreviousCalculation("-");
            denominationResult.setDifferenceMap(new LinkedHashMap<>());
        }
        
        return denominationResult;
    }

    private void saveCalculation(long amountInCents, Map<String, Integer> denominations) {
        CalculationHistory history = new CalculationHistory(amountInCents, denominations);
        calculationHistoryRepository.save(history);
    }

    private void addComparisonWithPrevious(DenominationResult result) {
        List<CalculationHistory> history = calculationHistoryRepository.findAllOrderByCalculationTimeDesc();
        
        if (history.size() > 1) {
            // Aktuelle Berechnung ist die erste, vorherige ist die zweite
            CalculationHistory previous = history.get(1);
            result.setPreviousCalculation(String.valueOf(previous.getAmountInCents()));
            result.setDifferenceMap(calculateDifference(result.getDenominations(), 
                previous.getDenominations()));
        } else {
            result.setPreviousCalculation("-");
            result.setDifferenceMap(new LinkedHashMap<>());
        }
    }

    private Map<String, Integer> calculateDifference(Map<String, Integer> current, Map<String, Integer> previous) {
        Map<String, Integer> differenceMap = new LinkedHashMap<>();
        
        // Alle möglichen Denominationen durchgehen
        Set<String> allDenominations = new HashSet<>();
        allDenominations.addAll(current.keySet());
        allDenominations.addAll(previous.keySet());
        
        for (String denomination : allDenominations) {
            int currentCount = current.getOrDefault(denomination, 0);
            int previousCount = previous.getOrDefault(denomination, 0);
            int difference = currentCount - previousCount;
            
            // Nur hinzufügen wenn es einen Unterschied gibt oder beide Werte vorhanden sind
            if (currentCount != 0 || previousCount != 0) {
                differenceMap.put(denomination, difference);
            }
        }
        
        return differenceMap;
    }

    public List<CalculationHistory> getCalculationHistory() {
        return calculationHistoryRepository.findAllOrderByCalculationTimeDesc();
    }

    public long parseAmountToCents(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be empty");
        }
        
        // Entferne Euro-Symbol und Leerzeichen
        String cleanAmount = amount.trim();
        
        // Validiere Format: darf nur Zahlen, ein Komma oder Punkt enthalten
        if (!cleanAmount.matches("^\\d+([.,]\\d{1,2})?$")) {
            throw new IllegalArgumentException("Invalid amount format. Use format like '123,45' or '123.45'");
        }
        
        // Ersetze Komma durch Punkt für einheitliche Verarbeitung
        cleanAmount = cleanAmount.replace(",", ".");
        
        try {
            // Parse als String um Rundungsfehler zu vermeiden
            String[] parts = cleanAmount.split("\\.");
            
            // Validiere Euro-Teil
            if (parts[0].isEmpty()) {
                throw new IllegalArgumentException("Euro part cannot be empty");
            }
            
            long euros = Long.parseLong(parts[0]);
            
            // Validiere Cent-Teil
            long cents = 0;
            if (parts.length > 1) {
                String centsStr = parts[1];
                
                // Validiere: maximal 2 Stellen nach dem Komma
                if (centsStr.length() > 2) {
                    throw new IllegalArgumentException("Maximum 2 decimal places allowed");
                }
                
                // Padding für einstellige Cent-Werte
                if (centsStr.length() == 1) {
                    centsStr += "0"; // 5.3 -> 5.30
                }
                
                cents = Long.parseLong(centsStr);
            }

            // Validiere Gesamtbetrag (maximal 999999,99€)
            long totalCents = euros * 100 + cents;
            if (totalCents < 0) {
                throw new IllegalArgumentException("Amount cannot be negative");
            }
            if (totalCents > 99999999) { // 999999,99€ in Cent
                throw new IllegalArgumentException("Maximum amount is 999999,99€");
            }
            
            return totalCents;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + amount);
        }
    }
}
