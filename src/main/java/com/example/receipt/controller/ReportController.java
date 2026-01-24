package com.example.receipt.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.receipt.dto.YearlyReportRequest;
import com.example.receipt.dto.YearlyReportResponse;
import com.example.receipt.messaging.ReportMessageProducer;
import com.example.receipt.repository.UserRepository;
import com.example.receipt.entity.User;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportMessageProducer reportMessageProducer;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/yearly")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> generateYearlyReport(
            @RequestParam @NotBlank String propertyName,
            @RequestParam @NotNull Integer year,
            Authentication authentication) {
        
        try {
            // Validate year
            if (year < 1900 || year > 2100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid year. Must be between 1900 and 2100.");
            }

            // Get current authenticated user
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create report request
            YearlyReportRequest reportRequest = new YearlyReportRequest(
                    propertyName,
                    year,
                    user.getEmail(),
                    user.getId()
            );

            // Send to RabbitMQ for asynchronous processing
            reportMessageProducer.sendReportRequest(reportRequest);

            // Return immediate response to client
            String reportId = UUID.randomUUID().toString();
            YearlyReportResponse response = new YearlyReportResponse(
                    "Report generation started. You will receive the PDF via email shortly.",
                    "PROCESSING",
                    reportId
            );

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + ex.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> reportServiceHealth() {
        return ResponseEntity.ok("Report service is running");
    }
}
