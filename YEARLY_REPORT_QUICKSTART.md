# Yearly Report Feature - Quick Start Guide

## Prerequisites Checklist

- [ ] Java 17 installed
- [ ] MySQL 8.0 running
- [ ] RabbitMQ 3.x running
- [ ] SendGrid account created
- [ ] SendGrid API key obtained

---

## 1. Configure SendGrid

### Get Your API Key

1. Visit https://sendgrid.com/login
2. Go to **Settings → API Keys**
3. Click **Create API Key**
4. Name it "Receipt System"
5. Select **Restricted Access**
6. Enable only "Mail Send"
7. Copy the generated key

### Update application.properties

```properties
sendgrid.api.key=SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
sendgrid.from.email=noreply@yourdomain.com
sendgrid.from.name=Receipt System
```

---

## 2. Start RabbitMQ

### Using Docker Compose

```bash
cd E:\development\receipt\receipt
docker compose up rabbitmq -d
```

### Verify RabbitMQ is Running

Visit: http://localhost:15672
- Username: guest
- Password: guest

You should see RabbitMQ Management Dashboard

---

## 3. Start Application

```bash
cd E:\development\receipt\receipt

# Build
.\mvnw.cmd clean package -DskipTests

# Run
java -jar target/receipt-0.0.1-SNAPSHOT.jar
```

Check logs for:
```
Started ReceiptApplication in X.XXX seconds
```

---

## 4. Create Test Data

### Insert Sample Property

```sql
INSERT INTO properties (name, street_number, street_name, city, state, zip_code, unit, alias)
VALUES ('123 Main Street', '123', 'Main Street', 'Boston', 'MA', '02101', NULL, NULL);
```

### Insert Sample Receipts

```sql
INSERT INTO receipts (description, amount, receipt_date, receipt_year)
VALUES 
  ('Property Tax Q1', 500.00, '2024-01-15 10:30:00', 2024),
  ('Utilities January', 120.50, '2024-01-20 14:00:00', 2024),
  ('Property Tax Q2', 500.00, '2024-04-15 09:00:00', 2024),
  ('Utilities April', 135.75, '2024-04-25 15:30:00', 2024),
  ('Property Tax Q3', 500.00, '2024-07-15 10:30:00', 2024),
  ('Utilities July', 145.25, '2024-07-28 13:00:00', 2024),
  ('Property Tax Q4', 500.00, '2024-10-15 09:45:00', 2024),
  ('Utilities October', 128.00, '2024-10-30 14:30:00', 2024);
```

### Link Receipts to Property

```sql
-- Get property ID and receipt IDs first
SELECT id FROM properties WHERE name = '123 Main Street';
SELECT id FROM receipts WHERE receipt_year = 2024;

-- Insert property receipts (assuming property_id=1, receipts have ids 1-8)
INSERT INTO property_receipts (property_id, receipt_id, portion)
VALUES 
  (1, 1, 500.00),
  (1, 2, 120.50),
  (1, 3, 500.00),
  (1, 4, 135.75),
  (1, 5, 500.00),
  (1, 6, 145.25),
  (1, 7, 500.00),
  (1, 8, 128.00);
```

---

## 5. Create User Account

### Register User

```bash
curl -X POST "http://localhost:8080/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "your_email@gmail.com",
    "password": "TestPassword123"
  }'
```

### Login to Get JWT Token

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "TestPassword123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwMTk4NDUwMCwiZXhwIjoxNzAyMDcwOTAwfQ.vNkMhNKw7Ow5...",
  "type": "Bearer",
  "id": 1,
  "username": "john_doe",
  "email": "your_email@gmail.com"
}
```

**Save the token:** You'll use it for the next request

---

## 6. Request Yearly Report

### Generate Report for 2024

```bash
curl -X POST "http://localhost:8080/api/reports/yearly?propertyName=123%20Main%20Street&year=2024" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json"
```

### Success Response (202 Accepted)

```json
{
  "message": "Report generation started. You will receive the PDF via email shortly.",
  "status": "PROCESSING",
  "reportId": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 7. Check Email

### Wait for Email Delivery

- Allow 5-10 seconds for RabbitMQ to process
- Check your email inbox (and spam folder)

### You Should Receive

**Subject:** `Yearly Receipt Report - 123 Main Street (2024)`

**Body:**
- Professional HTML formatted email
- Summary with total receipts and amount
- Detailed table of all receipts

**Attachment:** `123_Main_Street_Report_2024.pdf`
- Download and open in any PDF viewer
- Contains formatted report with all receipt details

---

## 8. View RabbitMQ Queues

### Check Queue Status

1. Go to http://localhost:15672
2. Login with guest/guest
3. Go to **Queues** tab
4. Look for `report.queue`
5. Should show:
   - Messages: 0 (or processing)
   - Consumers: 1

---

## Common Issues & Solutions

### Issue: "Email not received"

**Solution:**
1. Verify SendGrid API key is correct
2. Check **Spam/Promotions** folder
3. Check SendGrid Activity in dashboard: https://app.sendgrid.com/email_activity
4. Look for bounce/delivery errors

### Issue: "Property not found"

**Solution:**
1. Verify property name matches exactly (case-sensitive)
2. Run: `SELECT * FROM properties;` in MySQL
3. Use exact name from database

### Issue: "No receipts found for year"

**Solution:**
1. Check receipts exist: `SELECT * FROM receipts WHERE receipt_year = 2024;`
2. Check they're linked: `SELECT * FROM property_receipts;`
3. Verify property_id and receipt_id are correct

### Issue: "RabbitMQ connection refused"

**Solution:**
1. Check RabbitMQ is running: `docker ps | grep rabbitmq`
2. Verify port 5672 is open: `netstat -an | findstr 5672`
3. Restart: `docker compose restart rabbitmq`

---

## Advanced Usage

### Generate Multiple Reports

```bash
# Report for 2023
curl -X POST "http://localhost:8080/api/reports/yearly?propertyName=123%20Main%20Street&year=2023" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"

# Report for 2024
curl -X POST "http://localhost:8080/api/reports/yearly?propertyName=123%20Main%20Street&year=2024" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"

# Different property
curl -X POST "http://localhost:8080/api/reports/yearly?propertyName=Downtown%20Complex&year=2024" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### Monitor RabbitMQ Activity

Watch queue messages:
```bash
# Terminal 1: Watch queue
watch -n 1 'curl -s http://localhost:15672/api/queues | jq'

# Terminal 2: Generate report
curl -X POST "http://localhost:8080/api/reports/yearly?propertyName=123%20Main%20Street&year=2024" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### View Application Logs

```bash
# See real-time logs
tail -f target/receipt.log

# Look for messages:
# "Report request sent to RabbitMQ"
# "Processing report request for property"
# "Report sent successfully"
```

---

## Architecture Summary

```
1. User requests report
        ↓
2. Controller validates input
        ↓
3. Producer sends to RabbitMQ
        ↓
4. Consumer receives message
        ↓
5. Query database for receipts
        ↓
6. Generate PDF with iText
        ↓
7. Build HTML email
        ↓
8. Send via SendGrid API
        ↓
9. Email arrives in user inbox
```

**Total Time:** 2-5 seconds end-to-end

---

## Testing Checklist

- [ ] SendGrid API key is valid
- [ ] RabbitMQ is running
- [ ] Application started without errors
- [ ] User created and logged in
- [ ] Test property and receipts in database
- [ ] First report requested and received
- [ ] PDF opens and displays correctly
- [ ] Email HTML formatting looks good
- [ ] All receipt data is accurate

---

## Next Steps

1. **Production Deployment:**
   - Use environment variables for secrets
   - Enable logging and monitoring
   - Set up error alerts

2. **Enhance Features:**
   - Add scheduled reports (monthly, quarterly)
   - Multiple recipient support
   - Custom report templates
   - Report status tracking

3. **Scale System:**
   - Multiple consumer instances
   - Load balancer for API
   - Centralized logging (ELK stack)
   - Metrics collection (Prometheus)

---

## Support

For issues or questions:
1. Check logs: `tail -f target/receipt.log`
2. Monitor RabbitMQ: http://localhost:15672
3. Verify database: `SELECT * FROM properties;`
4. Test SendGrid: https://app.sendgrid.com/email_activity
