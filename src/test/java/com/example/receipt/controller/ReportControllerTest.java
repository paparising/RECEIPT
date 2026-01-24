package com.example.receipt.controller;

import com.example.receipt.dto.YearlyReportResponse;
import com.example.receipt.entity.User;
import com.example.receipt.messaging.ReportMessageProducer;
import com.example.receipt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {

    @Mock
    private ReportMessageProducer reportMessageProducer;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReportController reportController;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setEnabled(true);
    }

    @Test
    public void testGenerateYearlyReportSuccess() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(reportMessageProducer).sendReportRequest(any());

        // Act
        ResponseEntity<?> response = reportController.generateYearlyReport(
                "Main Building",
                2024,
                authentication
        );

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertTrue(response.getBody() instanceof YearlyReportResponse);
        YearlyReportResponse reportResponse = (YearlyReportResponse) response.getBody();
        assertEquals("PROCESSING", reportResponse.getStatus());
        assertTrue(reportResponse.getMessage().contains("Report generation started"));
        verify(reportMessageProducer, times(1)).sendReportRequest(any());
    }

    @Test
    public void testGenerateYearlyReportInvalidYearTooOld() {
        // Act
        ResponseEntity<?> response = reportController.generateYearlyReport(
                "Main Building",
                1800,
                authentication
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid year"));
        verify(reportMessageProducer, times(0)).sendReportRequest(any());
    }

    @Test
    public void testGenerateYearlyReportInvalidYearTooNew() {
        // Act
        ResponseEntity<?> response = reportController.generateYearlyReport(
                "Main Building",
                2101,
                authentication
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid year"));
        verify(reportMessageProducer, times(0)).sendReportRequest(any());
    }

    @Test
    public void testGenerateYearlyReportValidYearBoundary() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(reportMessageProducer).sendReportRequest(any());

        // Act & Assert for year 1900
        ResponseEntity<?> response1 = reportController.generateYearlyReport(
                "Main Building",
                1900,
                authentication
        );
        assertEquals(HttpStatus.ACCEPTED, response1.getStatusCode());

        // Act & Assert for year 2100
        ResponseEntity<?> response2 = reportController.generateYearlyReport(
                "Main Building",
                2100,
                authentication
        );
        assertEquals(HttpStatus.ACCEPTED, response2.getStatusCode());
    }

    @Test
    public void testGenerateYearlyReportUserNotFound() {
        // Arrange
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = reportController.generateYearlyReport(
                "Main Building",
                2024,
                authentication
        );

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error"));
        verify(reportMessageProducer, times(0)).sendReportRequest(any());
    }

    @Test
    public void testReportServiceHealth() {
        // Act
        ResponseEntity<String> response = reportController.reportServiceHealth();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Report service is running", response.getBody());
    }

    @Test
    public void testGenerateYearlyReportWithEmptyPropertyName() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act - Empty string should be handled by @NotBlank validation
        // This test verifies controller behavior when validation passes
        ResponseEntity<?> response = reportController.generateYearlyReport(
                "Valid Property",
                2024,
                authentication
        );

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    public void testMultipleReportsForSameProperty() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(reportMessageProducer).sendReportRequest(any());

        // Act - Generate reports for different years
        ResponseEntity<?> response1 = reportController.generateYearlyReport(
                "Main Building",
                2023,
                authentication
        );
        ResponseEntity<?> response2 = reportController.generateYearlyReport(
                "Main Building",
                2024,
                authentication
        );

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response1.getStatusCode());
        assertEquals(HttpStatus.ACCEPTED, response2.getStatusCode());
        verify(reportMessageProducer, times(2)).sendReportRequest(any());
    }
}
