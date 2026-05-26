package com.bijlipay.util;

public final class Constant {
    public static final String home = System.getProperty("user.home");
    // TODO Forgot password url
    public static final String FORGOT_PASSWORD_MAIL_STATIC_IP = "http://13.232.119.156/frontend/#/passwordRecovery/";
    // TODO FILE PATH
    public static final String UPLOAD_DOCUMENT_DIR_MIS = "cusDir/";
    // PUSH NOTIFICATION REDIRECT SCREEN
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
    public static final String[] TRANSACTIONS_COLUMNS = {"Mid", "Tid", "TxnDate", "PaymentDate", "BankName", "CardNo", "RRN", "CardType", "MerchantName", "TxnAmount", "NetAmount", "Narration"};
    public static final String SHEETNAME = "Settlement_Data";
    public static final String QRSHEETNAME = "QR_TRANSACTION_DATA";
    public static final String[] SWITCH_HEADERS = {"TxnDate", "Mid", "Tid", "TxnType", "RRN", "TxnAmount"};
    public static final String[] QR_HEADERS = {"TxnDate", "Mid", "Vpa", "gateWayResponseCode", "RRN", "TxnAmount"};
    public static final String[] MIS_HEADERS = {"TxnDate", "Mid", "Tid", "RRN", "TxnAmount"};
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
    //DESC
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


    public static String commonType(int type) {
        String ftype = null;
        if (RISH_HOLD_ID == type) {
            ftype = PUSH_NOTIFICATION_REDIRECT_TO_RISH_HOLD;
        } else if (PAPER_ROLL_ID == type) {
            ftype = PUSH_NOTIFICATION_REDIRECT_TO_PAPER_ROLL;
        } else if (OFFERS_ID == type) {
            ftype = PUSH_NOTIFICATION_REDIRECT_TO_OFFERS;
        } else if (SETTLEMENT_ID == type) {
            ftype = PUSH_NOTIFICATION_REDIRECT_TO_SETTLEMENT;
        } else if (TICKET_ID == type) {
            ftype = PUSH_NOTIFICATION_REDIRECT_TO_TICKET;
        } else if (TRANSACTION_ID == type) {
            ftype = PUSH_NOTIFICATION_REDIRECT_TO_TRANSACTION;
        }
        return ftype;
    }

    public static String getTxnType(String type) {
        String txnType = null;
        switch (type) {
            case MTI_PREAUTH:
                txnType = MTI_PREAUTH_DESC;
                break;
            case MTI_CARD_SALE:
                txnType = MTI_CARD_SALE_DESC;
                break;
            case MTI_VOID:
                txnType = MTI_VOID_DESC;
                break;
            case MTI_CASH_POS:
                txnType = MTI_CASH_POS_DESC;
                break;
            case MTI_PREAUTH_COMPLETION:
                txnType = MTI_PREAUTH_COMPLETION_DESC;
                break;
            case MTI_TIP_ADJUSTMENT:
                txnType = MTI_TIP_ADJUSTMENT_DESC;
                break;
            case MTI_OFFLINE_REFUND:
                txnType = MTI_OFFLINE_REFUND_DESC;
                break;
            case MTI_BATCH_UPLOAD:
                txnType = MTI_BATCH_UPLOAD_DESC;
                break;
            case MTI_REVERSAL:
                txnType = MTI_REVERSAL_DESC;
                break;
            case MTI_SETTLEMENT:
                txnType = MTI_SETTLEMENT_DESC;
                break;
            case MTI_UPI_37, MTI_UPI_39:
                txnType = MTI_UPI_DESC;
                break;
            default:
                txnType = "Others";
                break;
        }
        return txnType;
    }

//    public static String getTxnTypeStatus(String type) {
//        String txnType = null;
//        switch (type) {
//            case "02000000", "01003700", "01003900":
//                txnType = "Success";
//                break;
//            case "02000200":
//                txnType = "Void";
//                break;
//            case "04000000":
//                txnType ="Reversal";
//                break;
//            default:
//                txnType = "Failed";
//                break;
//        }
//        return txnType;
//    }

    public static String getTxnTypeStatus(String type) {
        return switch (type) {
            case "02000000", "01003700", "01003900" -> "Success";
            case "02000200" -> "Void";
            case "04000000" -> "Reversal";
            default -> "Failed";
        };
    }


    public interface AnalyticsInterface {
        String USER_LOGGED_IN = "Logged In";
    }

    public static final long OTP_VALIDATION = 3 * 60 * 1000;

    public static final String[] staticTxnHeader = {"customerVpa", "creditVpa", "merchantId", "merchantChannelId", "merchantTransactionId", "transactionTimestamp", "transactionAmount", "gatewayTransactionId", "gatewayResponseCode", "gatewayResponseMessage", "rrn", "checksum", "payeractype", "purposeCode", "initMode"};


    //Fiter Action
    public static final int TXN_RESPONSE_CODE = 1;
    public static final int TXN_TYPE = 2;
    public static final int DATE_RANGE = 3;
    public static final int ALL_FILTER = 4;

    //Date Range Filter
    public static final int DATE_RANGE_THIS_MONTH = 1;
    public static final int DATE_RANGE_LAST_30_DAYS = 2;
    public static final int DATE_RANGE_CHOOSE_DATE = 3;

    //Txn Status Value
    public static final int TXN_RESPONSE_SUCCESS = 1;
    public static final int TXN_RESPONSE_FAILURE = 2;
    public static final int TXN_RESPONSE_BOTH_SUCCESS_FAILURE = 3;

    /**
     * Action
     * 1 - Txn Status
     * Value
     * 1 - Success
     * 2 - Failure
     * 2 - Txn Type
     * Value
     * 1 - Upi
     * 2 - Card
     * 3 - Void
     * 3 - Date Range
     * Value
     * 1 - This Month Data
     * 2 - Last 30 Days Data
     * 3 - Choose Date
     * 4 - All Actions
     */

    public static final int RISK_HOLD_STATUS_COMPLETED = 1;
    public static final int RISK_HOLD_STATUS_PENDING = 2;

    public static final String RISK_HOLD_COMPLETED = "Release";

    //Card Type
    public static final String CARD_TYPE_VISA = "VISA";
    public static final String CARD_TYPE_DISCOVER = "RUPAY";
    public static final String CARD_TYPE_MASTER = "MASTERCARD";
    public static final String CARD_TYPE_AE = "AMEX";
    public static final String CARD_TYPE_JCB = "RUPAY";
    public static final String CARD_TYPE_UP = "RUPAY";
    public static final String CARD_TYPE_DINER = "RUPAY";

    public static final String CATEGORY_TYPE_TRANSACTION ="Transaction";
    public static final String CATEGORY_TYPE_SETTLEMENT ="Settlement";


}
