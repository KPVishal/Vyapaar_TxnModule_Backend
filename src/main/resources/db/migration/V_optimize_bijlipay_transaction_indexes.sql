-- ============================================================================
-- Performance indexes for bijlipay_transaction table
-- Run this on your MySQL database to optimize query performance.
-- ============================================================================

-- 1. Primary composite index: covers most queries that filter by terminal_id + response_code + date range
--    Supports: all paginated transaction listing, success/failure filters, date range queries
CREATE INDEX idx_bt_tid_respcode_resptime
    ON bijlipay_transaction (terminal_id, txn_response_code, response_received_time DESC);

-- 2. Terminal + date range: covers queries that filter by terminal_id + date range without response_code
--    Supports: "all status" queries, ORDER BY response_received_time DESC
CREATE INDEX idx_bt_tid_resptime
    ON bijlipay_transaction (terminal_id, response_received_time DESC);

-- 3. Covers the mti='0100' dedup subquery (the biggest bottleneck)
--    SELECT MAX(transaction_id) FROM bijlipay_transaction WHERE mti='0100' AND terminal_id=? GROUP BY terminal_id, stan
CREATE INDEX idx_bt_mti_tid_stan_txnid
    ON bijlipay_transaction (mti, terminal_id, stan, transaction_id);

-- 4. Covers the duplicate-RRN exclusion subquery
--    SELECT rrn_number WHERE terminal_id IN (?) AND rrn_number IS NOT NULL AND mti != '0320' GROUP BY rrn_number HAVING COUNT(*) > 1
CREATE INDEX idx_bt_tid_rrn_mti
    ON bijlipay_transaction (terminal_id, rrn_number, mti);

-- 5. Covers today/yesterday aggregate queries: terminal_id + mti + txn_response_code + txn_type
CREATE INDEX idx_bt_tid_mti_respcode_type
    ON bijlipay_transaction (terminal_id, mti, txn_response_code, txn_type);
