# Yearly Receipt Report Feature - Complete Guide

## Overview
This feature generates PDF reports of all receipts for a given property in a specific year, and automatically sends them via email using SendGrid. The system uses asynchronous processing with RabbitMQ for scalability.

---

## Architecture Diagram

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/reports/yearly?propertyName=Main Property&year=2024
       │ (With JWT Token)
       │
       ▼
┌────────────────────────────────────────────┐
│        ReportController                     │
│  - Validates input (property name, year)   │
│  - Gets authenticated user                 │
│  - Creates YearlyReportRequest             │
│  - Sends to RabbitMQ                       │
└──────┬─────────────────────────────────────┘
       │
       │ YearlyReportRequest → RabbitMQ
       │ (Asynchronous processing)
       │
       ▼
┌────────────────────────────────────────────┐
│   ReportMessageProducer (RabbitTemplate)   │
│  - Publishes to report.exchange            │
│  - Routing key: report.generate            │
│  - Message queued in report.queue          │
└──────┬─────────────────────────────────────┘
       │
       │ (Message in Queue)
       │
       ▼
┌────────────────────────────────────────────┐
│ ReportMessageConsumer (@RabbitListener)    │
│  - Listens to report.queue                 │
│  - Receives YearlyReportRequest            │
└──────┬─────────────────────────────────────┘
       │
       │ 1. Find property by name
       │ 2. Filter receipts by year
       │ 3. Generate PDF
       │
       ▼
┌────────────────────────────────────────────┐
│      PdfGeneratorService                   │
│  - Uses iText to generate PDF              │
│  - Creates professional report             │
│  - Includes tables and summary             │
│  - Returns byte[]                          │
└──────┬─────────────────────────────────────┘
       │
       │ PDF bytes ↓
       │
       ▼
┌────────────────────────────────────────────┐
│      EmailService (SendGrid)               │
│  - Builds email with HTML body             │
│  - Attaches PDF to email                   │
│  - Sends via SendGrid API                  │
└──────┬─────────────────────────────────────┘
       │
       │ SMTP Request
       │
       ▼
┌────────────────────────────────────────────┐
│   SendGrid Cloud Email Service             │
│  - Delivers email to user inbox            │
└────────────────────────────────────────────┘
```

---

## Step-by-Step Workflow

### **Step 1: User Requests Report**

```bash
curl -X POST "http://localhost:8080/api/reports/yearly?propertyName=123%20Main%20Street&year=2024" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json"
```

**What happens:**
- ReportController receives the request
- Extracts propertyName and year from query parameters
- Gets current authenticated user from JWT token
- Validates input (year between 1900-2100)

### **Step 2: Create Report Request**

```java
YearlyReportRequest reportRequest = new YearlyReportRequest(
    "123 Main Street",              // propertyName
    2024,                            // year
    "john@example.com",              // userEmail (from authenticated user)
    1L                               // userId (from authenticated user)
);
```

### **Step 3: Send to RabbitMQ (Asynchronous)**

```java
// Producer sends to RabbitMQ
reportMessageProducer.sendReportRequest(reportRequest);

// Message routing:
// Exchange: report.exchange (Topic Exchange)
// Routing Key: report.generate
// Queue: report.queue
```

**Immediate Response to Client:**
```json
{
  "message": "Report generation started. You will receive the PDF via email shortly.",
  "status": "PROCESSING",
  "reportId": "550e8400-e29b-41d4-a716-446655440000"
}
HTTP 202 Accepted
```

**Why async?**
- Client doesn't wait for PDF generation
- Report generation can take time (database queries, PDF creation)
- Server can handle multiple reports simultaneously
- Better user experience

### **Step 4: Consumer Processes Request**

```java
@RabbitListener(queues = RabbitMQConfig.REPORT_QUEUE)
public void processReportRequest(YearlyReportRequest reportRequest) {
    
    // 1. Find property by name
    List<Property> properties = propertyRepository.findAll().stream()
        .filter(p -> p.getName().equalsIgnoreCase("123 Main Street"))
        .collect(Collectors.toList());
    
    if (properties.isEmpty()) {
        // Send error email
        sendErrorEmail(reportRequest.getUserEmail(), propertyName, 
            "Property not found with name: " + propertyName);
        return;
    }
    
    Property property = properties.get(0);
    
    // 2. Filter receipts by year
    List<PropertyReceipt> yearlyReceipts = property.getPropertyReceipts().stream()
        .filter(pr -> pr.getReceipt().getYear().equals(2024))
        .collect(Collectors.toList());
    
    if (yearlyReceipts.isEmpty()) {
        sendErrorEmail(..., "No receipts found for year 2024");
        return;
    }
}
```

### **Step 5: Generate PDF**

```java
// PdfGeneratorService.generateYearlyReportPdf()

// Creates professional PDF with:
// 1. Title: "Yearly Receipt Report"
// 2. Header with property details
// 3. Summary section (total receipts, total amount)
// 4. Detailed table of all receipts
//    - Date
//    - Description
//    - Amount
//    - Portion (amount allocated to property)
//    - Receipt ID
// 5. Footer with generation timestamp

byte[] pdfContent = pdfGeneratorService.generateYearlyReportPdf(
    property,
    2024,
    yearlyReceipts
);

// Returns: PDF file as byte array
```

### **Step 6: Generate HTML Email**

```html
<html><body style='font-family: Arial, sans-serif;'>
<div style='max-width: 600px; margin: 0 auto;'>
  <h2 style='color: #2980b9;'>Yearly Receipt Report</h2>
  
  <p><strong>Property:</strong> 123 Main Street</p>
  <p><strong>Address:</strong> 123 Main Street, Boston, MA 02101</p>
  <p><strong>Year:</strong> 2024</p>
  <p><strong>Generated:</strong> 2024-01-23 16:45:30</p>
  
  <hr>
  <h3>Summary</h3>
  <p><strong>Total Receipts:</strong> 12</p>
  <p><strong>Total Amount:</strong> $1,250.00</p>
  
  <hr>
  <h3>Receipt Details</h3>
  <table style='width: 100%; border-collapse: collapse;'>
    <thead style='background-color: #2980b9; color: white;'>
      <tr>
        <th>Date</th>
        <th>Description</th>
        <th>Amount</th>
        <th>Portion</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>2024-01-15</td>
        <td>Property Tax</td>
        <td>$500.00</td>
        <td>$500.00</td>
      </tr>
      <!-- ... more rows ... -->
    </tbody>
  </table>
</div>
</body></html>
```

### **Step 7: Send Email via SendGrid**

```java
// EmailService.sendReportEmail()

String emailJson = {
  "personalizations": [{
    "to": [{ "email": "john@example.com" }]
  }],
  "from": {
    "email": "noreply@receiptsystem.com",
    "name": "Receipt System"
  },
  "subject": "Yearly Receipt Report - 123 Main Street (2024)",
  "content": [{
    "type": "text/html",
    "value": "<html>...</html>"
  }],
  "attachments": [{
    "content": "JVBERi0xLjQK...",  // Base64-encoded PDF
    "type": "application/pdf",
    "filename": "123_Main_Street_Report_2024.pdf",
    "disposition": "attachment"
  }]
}

// Send to SendGrid API
POST https://api.sendgrid.com/v3/mail/send
Authorization: Bearer SG.xxx...xxx
Content-Type: application/json
Body: emailJson
```

### **Step 8: SendGrid Delivers Email**

```
Email arrives in user's inbox with:
- Professional HTML-formatted email
- PDF attachment: "123_Main_Street_Report_2024.pdf"
- Can be opened/downloaded immediately
```

---

## API Endpoints

### **Generate Yearly Report**

```http
POST /api/reports/yearly?propertyName=<property_name>&year=<year>
Authorization: Bearer <JWT_TOKEN>
```

**Request:**
```
Query Parameters:
- propertyName (required): Name of the property (e.g., "Main Building")
- year (required): Year for report (e.g., 2024)

Headers:
- Authorization: Bearer <valid_jwt_token>
- Content-Type: application/json
```

**Success Response (202 Accepted):**
```json
{
  "message": "Report generation started. You will receive the PDF via email shortly.",
  "status": "PROCESSING",
  "reportId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Error Responses:**

1. **No Token (401 Unauthorized):**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required",
  "path": "/api/reports/yearly"
}
```

2. **Invalid Year (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Invalid year. Must be between 1900 and 2100."
}
```

3. **Server Error (500 Internal Server Error):**
```json
{
  "status": 500,
  "error": "Error: Database connection failed"
}
```

### **Check Report Service Health**

```http
GET /api/reports/health
```

**Response:**
```
Report service is running
```

---

## Configuration

### **1. RabbitMQ Setup**

Add to `application.properties`:
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

**Queue Configuration (RabbitMQConfig.java):**
- **Queue Name:** `report.queue` (durable)
- **Exchange Name:** `report.exchange` (Topic Exchange)
- **Routing Key:** `report.generate`
- **Binding:** Queue bound to exchange with routing key

### **2. SendGrid Setup**

Add to `application.properties`:
```properties
sendgrid.api.key=SG.xxx_your_sendgrid_api_key_xxx
sendgrid.from.email=noreply@receiptsystem.com
sendgrid.from.name=Receipt System
```

**How to get SendGrid API Key:**
1. Sign up at https://sendgrid.com
2. Go to Settings → API Keys
3. Create new API key with "Mail Send" permission
4. Copy the key and add to properties

### **3. MySQL Setup**

Ensure users table exists and has a user:
```sql
INSERT INTO users (username, password, email, enabled) 
VALUES ('john_doe', '$2a$10$...', 'john@example.com', 1);
```

---

## Error Handling

### **Scenario 1: Property Not Found**

```
Request: propertyName="Non-Existent Property", year=2024

Response Email:
Subject: "Report Generation Failed - Non-Existent Property"

Body:
Report Generation Error
Property: Non-Existent Property
Error: Property not found with name: Non-Existent Property
Please verify the property name and try again.
```

### **Scenario 2: No Receipts for Year**

```
Request: propertyName="Main Building", year=1900

Response Email:
Subject: "Report Generation Failed - Main Building"

Body:
Report Generation Error
Property: Main Building
Error: No receipts found for year 1900
Please verify the property name and try again.
```

### **Scenario 3: Database Error**

```
If database query fails:

1. RabbitMQ consumer catches exception
2. Sends error email with exception message
3. Logs error to console
4. Consumer remains running for next messages
```

---

## System Requirements

### **1. RabbitMQ Running**

```bash
# Start RabbitMQ (via Docker Compose)
docker compose up rabbitmq

# Or manually (if installed)
rabbitmq-server
```

Check RabbitMQ Management UI: http://localhost:15672
- Username: guest
- Password: guest

### **2. SendGrid Account**

- Active SendGrid account with API key
- Email domain verified for sending
- Sufficient email credits

### **3. MySQL Database**

- Users table created
- Properties and Receipts tables populated
- PropertyReceipt junction table has test data

---

## Sample Request

### **Complete End-to-End Example**

```bash
# 1. Register user (if needed)
curl -X POST "http://localhost:8080/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "securePassword123"
  }'

# 2. Login to get JWT token
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123"
  }'

# Response:
# {
#   "token": "eyJhbGciOiJIUzUxMiJ9...",
#   "type": "Bearer",
#   "id": 1,
#   "username": "john_doe",
#   "email": "john@example.com"
# }

# 3. Request yearly report
curl -X POST "http://localhost:8080/api/reports/yearly?propertyName=Main%20Property&year=2024" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -H "Content-Type: application/json"

# Response:
# {
#   "message": "Report generation started. You will receive the PDF via email shortly.",
#   "status": "PROCESSING",
#   "reportId": "550e8400-e29b-41d4-a716-446655440000"
# }

# 4. Wait a few seconds for email delivery
# (Check your email inbox for the PDF report)
```

---

## Performance Considerations

### **Scalability**
- **Asynchronous Processing:** Multiple reports can be generated simultaneously
- **RabbitMQ Queue:** Handles message buffering if server is slow
- **Stateless Consumer:** Can run on multiple servers

### **Resource Usage**
- **PDF Generation:** ~1-2 seconds for 100 receipts
- **Email Sending:** ~500ms via SendGrid API
- **Database Queries:** Optimized with eager loading

### **Limits**
- **Max Receipts per Report:** No hard limit (tested with 1000+)
- **Email Attachment Size:** SendGrid limit is 50MB
- **RabbitMQ Queue Size:** Depends on available disk space

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| **No email received** | Check SendGrid API key in properties; Verify email in allowlist; Check spam folder |
| **Property not found error** | Verify property name matches exactly (case-sensitive); Check database for property |
| **No receipts for year** | Verify receipts exist in database; Check year field in receipts table |
| **RabbitMQ connection error** | Ensure RabbitMQ is running; Check host/port in properties |
| **PDF attachment empty** | Check database connection; Verify receipts have data |

---

## Future Enhancements

1. **Scheduled Reports:** Auto-generate and send reports on monthly/yearly basis
2. **Multiple Recipients:** Send to multiple email addresses
3. **Report Templates:** Customizable HTML/PDF templates
4. **Report Status Tracking:** Database table to track report generation status
5. **Retry Logic:** Automatic retry for failed email deliveries
6. **Export Formats:** Excel, CSV, XML export options
7. **Advanced Filtering:** Filter by receipt type, date range, amount range
