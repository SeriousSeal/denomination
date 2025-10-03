package com.example.eurodenomination.repository;

import com.example.eurodenomination.model.CalculationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculationHistoryRepository extends JpaRepository<CalculationHistory, Long> {
    
    @Query("SELECT c FROM CalculationHistory c ORDER BY c.calculationTime DESC")
    List<CalculationHistory> findAllOrderByCalculationTimeDesc();

}
