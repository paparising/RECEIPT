package com.example.receipt.messaging;

import com.example.receipt.config.RabbitMQConfig;
import com.example.receipt.dto.YearlyReportRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportMessageProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ReportMessageProducer reportMessageProducer;

    private YearlyReportRequest testRequest;

    @BeforeEach
    public void setUp() {
        testRequest = new YearlyReportRequest(
                "Main Building",
                2024,
                "user@example.com",
                1L
        );
    }

    @Test
    public void testSendReportRequestSuccess() {
        // Act
        reportMessageProducer.sendReportRequest(testRequest);

        // Assert - Verify RabbitTemplate.convertAndSend was called with correct parameters
        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<YearlyReportRequest> messageCaptor = ArgumentCaptor.forClass(YearlyReportRequest.class);

        verify(rabbitTemplate, times(1)).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                messageCaptor.capture()
        );

        assertEquals(RabbitMQConfig.REPORT_EXCHANGE, exchangeCaptor.getValue());
        assertEquals(RabbitMQConfig.REPORT_ROUTING_KEY, routingKeyCaptor.getValue());
        assertEquals("Main Building", messageCaptor.getValue().getPropertyName());
        assertEquals(2024, messageCaptor.getValue().getYear());
        assertEquals("user@example.com", messageCaptor.getValue().getUserEmail());
        assertEquals(1L, messageCaptor.getValue().getUserId());
    }

    @Test
    public void testSendReportRequestWithDifferentProperty() {
        // Arrange
        YearlyReportRequest anotherRequest = new YearlyReportRequest(
                "Downtown Office",
                2023,
                "admin@example.com",
                2L
        );

        // Act
        reportMessageProducer.sendReportRequest(anotherRequest);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.REPORT_EXCHANGE),
                eq(RabbitMQConfig.REPORT_ROUTING_KEY),
                eq(anotherRequest)
        );
    }

    @Test
    public void testSendMultipleReportRequests() {
        // Arrange
        YearlyReportRequest request1 = new YearlyReportRequest("Property1", 2024, "user1@example.com", 1L);
        YearlyReportRequest request2 = new YearlyReportRequest("Property2", 2023, "user2@example.com", 2L);
        YearlyReportRequest request3 = new YearlyReportRequest("Property3", 2022, "user3@example.com", 3L);

        // Act
        reportMessageProducer.sendReportRequest(request1);
        reportMessageProducer.sendReportRequest(request2);
        reportMessageProducer.sendReportRequest(request3);

        // Assert
        verify(rabbitTemplate, times(3)).convertAndSend(
                eq(RabbitMQConfig.REPORT_EXCHANGE),
                eq(RabbitMQConfig.REPORT_ROUTING_KEY),
                any(YearlyReportRequest.class)
        );
    }

    @Test
    public void testSendReportRequestWithNullPropertiesHandled() {
        // Arrange
        YearlyReportRequest nullRequest = new YearlyReportRequest(
                null,
                null,
                null,
                null
        );

        // Act
        reportMessageProducer.sendReportRequest(nullRequest);

        // Assert - Should still send the message (validation happens at controller level)
        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitMQConfig.REPORT_EXCHANGE,
                RabbitMQConfig.REPORT_ROUTING_KEY,
                nullRequest
        );
    }

    @Test
    public void testSendReportRequestExchangeConfiguration() {
        // Act
        reportMessageProducer.sendReportRequest(testRequest);

        // Assert - Verify correct exchange and routing key are used
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.REPORT_EXCHANGE),
                eq(RabbitMQConfig.REPORT_ROUTING_KEY),
                eq(testRequest)
        );
    }

    @Test
    public void testSendReportRequestPreservesAllData() {
        // Arrange
        String propertyName = "Complex Building Name with Spaces";
        Integer year = 2025;
        String email = "complex.email+tag@example.com";
        Long userId = 999L;

        YearlyReportRequest complexRequest = new YearlyReportRequest(
                propertyName,
                year,
                email,
                userId
        );

        // Act
        reportMessageProducer.sendReportRequest(complexRequest);

        // Assert - All data preserved through serialization
        ArgumentCaptor<YearlyReportRequest> messageCaptor = ArgumentCaptor.forClass(YearlyReportRequest.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.REPORT_EXCHANGE),
                eq(RabbitMQConfig.REPORT_ROUTING_KEY),
                messageCaptor.capture()
        );

        YearlyReportRequest captured = messageCaptor.getValue();
        assertEquals(propertyName, captured.getPropertyName());
        assertEquals(year, captured.getYear());
        assertEquals(email, captured.getUserEmail());
        assertEquals(userId, captured.getUserId());
    }
}
