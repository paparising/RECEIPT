package com.example.receipt.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.receipt.config.RabbitMQConfig;
import com.example.receipt.dto.YearlyReportRequest;

@Service
public class ReportMessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendReportRequest(YearlyReportRequest reportRequest) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.REPORT_EXCHANGE,
            RabbitMQConfig.REPORT_ROUTING_KEY,
            reportRequest
        );
        System.out.println("Report request sent to RabbitMQ: " + reportRequest.getPropertyName() + " for year " + reportRequest.getYear());
    }
}
