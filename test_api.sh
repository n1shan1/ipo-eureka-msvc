curl -X POST "http://localhost:8080/api/v1/ipo/IPO001/apply" -H "Content-Type: application/json" -H "Idempotency-Key: test-success-1" -d '{"investorId":"INV001","lots":1,"userUpiId":"test1@upi"}'
