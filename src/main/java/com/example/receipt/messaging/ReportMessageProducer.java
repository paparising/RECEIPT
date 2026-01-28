package com.example.receipt.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.receipt.config.RabbitMQConfig;
import com.example.receipt.dto.YearlyReportRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public ResponseEntity<?> sendReportRequest(YearlyReportRequest reportRequest) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.REPORT_EXCHANGE,
                RabbitMQConfig.REPORT_ROUTING_KEY,
                reportRequest
            );
            
            System.out.println("Report request sent to RabbitMQ: " + reportRequest.getPropertyName() + " for year " + reportRequest.getYear());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Report request accepted successfully");
            response.put("propertyName", reportRequest.getPropertyName());
            response.put("year", reportRequest.getYear().toString());
            response.put("status", "QUEUED");
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception ex) {
            System.err.println("Error sending report request: " + ex.getMessage());
            ex.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to queue report request");
            errorResponse.put("details", ex.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
