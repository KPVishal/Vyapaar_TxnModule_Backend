package com.bijlipay.util;


import static com.bijlipay.util.Constant.*;

public class CardUtil {


    public static boolean checkStringValue(String data) {
        return data != null && !data.equalsIgnoreCase("") && !data.equalsIgnoreCase("null") && !data.isEmpty();
    }

    public static String getCardType(int bin) {
        if (binRangeVisa(bin)) {
            return CARD_TYPE_VISA;
        } else if (binRangeMaster(bin)) {
            return CARD_TYPE_MASTER;
        } else if (binRangeDiscover(bin)) {
            return CARD_TYPE_DISCOVER;
        } else if (binRangeUnionPay(bin)) {
            return CARD_TYPE_UP;
        } else if (binRangeJcb(bin)) {
            return CARD_TYPE_JCB;
        } else {
            return " ";
        }
    }

    private static boolean binRangeVisa(int bin) {
        return bin >= 400000 && bin <= 499999;
    }

    private static boolean binRangeMaster(int bin) {
        return (bin >= 222100 && bin <= 272099) || (bin >= 510000 && bin <= 559999) || (bin >= 675920 && bin <= 675923);
    }

    private static boolean binRangeDiscover(int bin) {
        return (bin >= 300000 && bin <= 305999) || (bin >= 309500 && bin <= 309599) || (bin >= 360000 && bin <= 369999) ||
                (bin >= 380000 && bin <= 399999) || (bin >= 601100 && bin <= 601103) || (bin >= 601105 && bin <= 601109) ||
                (bin >= 601120 && bin <= 601149) || (bin == 601174) || (bin >= 601177 && bin <= 601179) ||
                (bin >= 601186 && bin <= 601199) || (bin >= 644000 && bin <= 650599) || (bin >= 650601 && bin <= 650609) ||
                (bin >= 650611 && bin <= 659999) || (bin >= 608001 && bin <= 608500) || (bin == 820199);
    }

    private static boolean binRangeUnionPay(int bin) {
        return (bin == 621094) || (bin >= 622126 && bin <= 622925) || (bin >= 622926 && bin <= 623796) ||
                (bin >= 624000 && bin <= 626999) || (bin >= 628200 && bin <= 628899) || (bin >= 810000 && bin <= 810999) ||
                (bin >= 811000 && bin <= 813199) || (bin >= 813200 && bin <= 815199) || (bin >= 815200 && bin <= 816399) ||
                (bin >= 816400 && bin <= 817199) || (bin >= 309600 && bin <= 310299) || (bin >= 311200 && bin <= 312099) ||
                (bin >= 315800 && bin <= 315999) || (bin >= 333700 && bin <= 334999) || (bin >= 352800 && bin <= 358999);
    }

    private static boolean binRangeJcb(int bin) {
        return (bin >= 308800 && bin <= 309499) || bin == 353014;
    }

    private static boolean binRangeAE(int bin) {
        return (bin >= 340000 && bin <= 349999) || (bin >= 370000 && bin <= 379999);
    }

//    public static int getCardDrawable(String cardName) {
//        switch (cardName) {
//            case Const.CARD_TYPE_VISA:
//                return R.drawable.ic_visa;
//            case Const.CARD_TYPE_MASTER:
//                return R.drawable.ic_mastercard;
//            case Const.CARD_TYPE_DISCOVER:
//                return R.drawable.ic_rupay;
//            // case Const.CARD_TYPE_JCB:
//            // case Const.CARD_TYPE_DINER:
//            case Const.CARD_TYPE_AE:
//                return R.drawable.ic_visa;
//            default:
//                return R.drawable.ic_card_default;
//        }
//    }
}
