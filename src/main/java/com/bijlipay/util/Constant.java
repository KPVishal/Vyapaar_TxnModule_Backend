package com.bijlipay.util;

public final class Constant {
    public static final String home = System.getProperty("user.home");
    public static final String FORGOT_PASSWORD_MAIL_STATIC_IP = "http://13.232.119.156/frontend/#/passwordRecovery/";
    public static final String UPLOAD_DOCUMENT_DIR_MIS = "cusDir/";
    public static final String PUSH_NOTIFICATION_REDIRECT_TO_RISH_HOLD = "RiskHold";
    public static final String PUSH_NOTIFICATION_REDIRECT_TO_OFFERS = "Offers";
    public static final String PUSH_NOTIFICATION_REDIRECT_TO_PAPER_ROLL = "PaperRoll";
    public static final String PUSH_NOTIFICATION_REDIRECT_TO_SETTLEMENT = "Settlement";
    public static final String PUSH_NOTIFICATION_REDIRECT_TO_TICKET = "Ticket";
    public static final String PUSH_NOTIFICATION_REDIRECT_TO_TRANSACTION = "Transaction";
    public static final int RISH_HOLD_ID = 1;
    public static final int PAPER_ROLL_ID = 2;
    public static final int OFFERS_ID = 3;
    public static final int SETTLEMENT_ID = 4;
    public static final int TICKET_ID = 5;
    public static final int TRANSACTION_ID = 6;
    public static final String[] TRANSACTIONS_COLUMNS = new String[]{"Mid", "Tid", "TxnDate", "PaymentDate", "BankName", "CardNo", "RRN", "CardType", "MerchantName", "TxnAmount", "NetAmount", "Narration"};
    public static final String SHEETNAME = "Settlement_Data";
    public static final String QRSHEETNAME = "QR_TRANSACTION_DATA";
    public static final String[] SWITCH_HEADERS = new String[]{"TxnDate", "Mid", "Tid", "TxnType", "RRN", "TxnAmount"};
    public static final String[] QR_HEADERS = new String[]{"TxnDate", "Mid", "Vpa", "gateWayResponseCode", "RRN", "TxnAmount"};
    public static final String[] MIS_HEADERS = new String[]{"TxnDate", "Mid", "Tid", "RRN", "TxnAmount"};
    public static final String MTI_PREAUTH = "010000";
    public static final String MTI_CARD_SALE = "020000";
    public static final String MTI_VOID = "020002";
    public static final String MTI_CASH_POS = "020009";
    public static final String MTI_PREAUTH_COMPLETION = "022000";
    public static final String MTI_TIP_ADJUSTMENT = "022002";
    public static final String MTI_OFFLINE_REFUND = "022020";
    public static final String MTI_BATCH_UPLOAD = "0320";
    public static final String MTI_REVERSAL = "0400";
    public static final String MTI_SETTLEMENT = "050092";
    public static final String MTI_UPI_37 = "010037";
    public static final String MTI_UPI_39 = "010039";
    public static final String MTI_PREAUTH_DESC = "Pre Auth";
    public static final String MTI_CARD_SALE_DESC = "Card Sale";
    public static final String MTI_VOID_DESC = "Void";
    public static final String MTI_CASH_POS_DESC = "Cash@Pos";
    public static final String MTI_PREAUTH_COMPLETION_DESC = "Pre Auth Completion";
    public static final String MTI_TIP_ADJUSTMENT_DESC = "Tip Adjustment";
    public static final String MTI_OFFLINE_REFUND_DESC = "Offline Refund";
    public static final String MTI_BATCH_UPLOAD_DESC = "Batch Upload";
    public static final String MTI_REVERSAL_DESC = "Reversal";
    public static final String MTI_SETTLEMENT_DESC = "Settlement";
    public static final String MTI_UPI_DESC = "DQR Sale";
    public static final long OTP_VALIDATION = 180000L;
    public static final String[] staticTxnHeader = new String[]{"customerVpa", "creditVpa", "merchantId", "merchantChannelId", "merchantTransactionId", "transactionTimestamp", "transactionAmount", "gatewayTransactionId", "gatewayResponseCode", "gatewayResponseMessage", "rrn", "checksum", "payeractype", "purposeCode", "initMode"};
    public static final int TXN_RESPONSE_CODE = 1;
    public static final int TXN_TYPE = 2;
    public static final int DATE_RANGE = 3;
    public static final int ALL_FILTER = 4;
    public static final int DATE_RANGE_THIS_MONTH = 1;
    public static final int DATE_RANGE_LAST_30_DAYS = 2;
    public static final int DATE_RANGE_CHOOSE_DATE = 3;
    public static final int TXN_RESPONSE_SUCCESS = 1;
    public static final int TXN_RESPONSE_FAILURE = 2;
    public static final int TXN_RESPONSE_BOTH_SUCCESS_FAILURE = 3;
    public static final int RISK_HOLD_STATUS_COMPLETED = 1;
    public static final int RISK_HOLD_STATUS_PENDING = 2;
    public static final String RISK_HOLD_COMPLETED = "Release";
    public static final String CARD_TYPE_VISA = "VISA";
    public static final String CARD_TYPE_DISCOVER = "RUPAY";
    public static final String CARD_TYPE_MASTER = "MASTERCARD";
    public static final String CARD_TYPE_AE = "AMEX";
    public static final String CARD_TYPE_JCB = "RUPAY";
    public static final String CARD_TYPE_UP = "RUPAY";
    public static final String CARD_TYPE_DINER = "RUPAY";
    public static final String CATEGORY_TYPE_TRANSACTION = "Transaction";
    public static final String CATEGORY_TYPE_SETTLEMENT = "Settlement";

    public static String commonType(int type) {
        String ftype = null;
        if (1 == type) {
            ftype = "RiskHold";
        } else if (2 == type) {
            ftype = "PaperRoll";
        } else if (3 == type) {
            ftype = "Offers";
        } else if (4 == type) {
            ftype = "Settlement";
        } else if (5 == type) {
            ftype = "Ticket";
        } else if (6 == type) {
            ftype = "Transaction";
        }

        return ftype;
    }

    public static String getTxnType(String type) {
        String txnType = null;
        switch (type) {
            case "010000":
                txnType = "Pre Auth";
                break;
            case "020000":
                txnType = "Card Sale";
                break;
            case "020002":
                txnType = "Void";
                break;
            case "020009":
                txnType = "Cash@Pos";
                break;
            case "022000":
                txnType = "Pre Auth Completion";
                break;
            case "022002":
                txnType = "Tip Adjustment";
                break;
            case "022020":
                txnType = "Offline Refund";
                break;
            case "0320":
                txnType = "Batch Upload";
                break;
            case "0400":
                txnType = "Reversal";
                break;
            case "050092":
                txnType = "Settlement";
                break;
            case "010037":
            case "010039":
                txnType = "DQR Sale";
                break;
            default:
                txnType = "Others";
        }

        return txnType;
    }

    public static String getTxnTypeStatus(String type) {
        String var10000;
        switch (type) {
            case "02000000":
            case "01003700":
            case "01003900":
                var10000 = "Success";
                break;
            case "02000200":
                var10000 = "Void";
                break;
            case "04000000":
                var10000 = "Reversal";
                break;
            default:
                var10000 = "Failed";
        }

        return var10000;
    }

    public interface AnalyticsInterface {
        String USER_LOGGED_IN = "Logged In";
    }
}