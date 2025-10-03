package com.example.eurodenomination.controller;

import com.example.eurodenomination.model.AmountRequest;
import com.example.eurodenomination.model.CalculationHistory;
import com.example.eurodenomination.model.DenominationResult;
import com.example.eurodenomination.service.DenominationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/denomination")
@CrossOrigin(origins = "*")
public class DenominationController {

    @Autowired
    private DenominationService denominationService;

    @PostMapping("/calculate")
    public ResponseEntity<DenominationResult> calculateDenomination(
            @RequestBody AmountRequest request,
            @RequestParam(value = "calculateDifference", defaultValue = "true") boolean calculateDifference) {
        try {
            // Konvertiere Euro-Betrag zu Cent (ohne Floats!)
            long amountInCents = denominationService.parseAmountToCents(request.getAmount());
            DenominationResult result = denominationService.calculateDenomination(amountInCents, calculateDifference);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<CalculationHistory>> getCalculationHistory() {
        List<CalculationHistory> history = denominationService.getCalculationHistory();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Euro Denomination API is running");
    }


}
