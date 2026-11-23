package com.bijlipay.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.time.DateUtils;

public class DateUtil {
    public static String yesterday() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.add(5, -1);
        String currentDate = formatter.format(cal.getTime());
        System.out.println("yesterday " + currentDate);
        return currentDate;
    }

    public static Map<String, String> getAbsoluteDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String initiateDate = dateFormatter.format(cal.getTime());
        cal.add(10, 1);
        String expiryDate = dateFormatter.format(cal.getTime());
        Map<String, String> dates = new HashMap();
        dates.put("initialDate", initiateDate);
        dates.put("expiryDate", expiryDate);
        return dates;
    }

    public static Date longtoDate(long convert) {
        Date date = new Date(convert * 1000L);
        SimpleDateFormat dateToString = new SimpleDateFormat("yyyy-MM-dd");
        Date rdate = null;

        try {
            rdate = dateToString.parse(dateToString.format(date));
            System.out.println("rdate " + String.valueOf(rdate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return rdate;
    }

    public static String longtoDate(long convert, String format) throws ParseException {
        Date date = new Date(convert * 1000L);
        SimpleDateFormat dateToString = new SimpleDateFormat("yyyy-MM-dd");
        Date rdate = null;

        try {
            rdate = dateToString.parse(dateToString.format(date));
            System.out.println("rdate " + String.valueOf(rdate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String formattedDate = (new SimpleDateFormat(format)).format(rdate);
        System.out.println("Formatted date : ------- : {}" + formattedDate);
        return formattedDate;
    }

    public static String longtoDateForQr(long convert, String format) throws ParseException {
        Date date = new Date(convert * 1000L);
        SimpleDateFormat dateToString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date rdate = null;

        try {
            rdate = dateToString.parse(dateToString.format(date));
            System.out.println("rdate " + String.valueOf(rdate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String formattedDate = (new SimpleDateFormat(format)).format(rdate);
        System.out.println("Formatted date : ------- : {}" + formattedDate);
        return formattedDate;
    }

    public static String currentDate(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date date = new Date();
        String currentDate = formatter.format(date);
        return currentDate;
    }

    public static Date convertStringToDate(String stringCellValue, String format) {
        if (stringCellValue != "") {
            SimpleDateFormat before = new SimpleDateFormat(format);
            Date date = null;

            try {
                date = before.parse(stringCellValue);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return date;
        } else {
            return null;
        }
    }

    public static String convertToString(Date paymentdate, String format) {
        SimpleDateFormat dateToString = new SimpleDateFormat(format);
        String strDate = dateToString.format(paymentdate);
        return strDate;
    }

    public static String getDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormatter.format(cal.getTime());
    }

    public static String getDateByFormat(String format) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat(format);
        return dateFormatter.format(cal.getTime());
    }

    public static Date currentDate() {
        Date date = new Date();
        return date;
    }

    public static Date lastThreeMonthDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(2, -3);
        Date date = cal.getTime();
        return date;
    }

    public static Date addOneDay(Date date) {
        Date newDate = DateUtils.addDays(new Date(), 1);
        return newDate;
    }

    public static Date stringToDate(String date) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = dateFormatter.parse(date);
        return date1;
    }

    public static String dateToString(Date date) throws ParseException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormatter.format(date);
    }

    public static String formattedDate(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = sdf.parse(time);
        return output.format(d);
    }

    public static String today() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        String currentDate = formatter.format(cal.getTime());
        return currentDate;
    }

    public static String stringToDateFroAxis(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, formatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String output = zonedDateTime.format(outputFormatter);
        return output;
    }

    public static String kvbDateFormat(String date) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(date, inputFormatter);
        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        String outputDateTimeString = zonedDateTime.format(outputFormatter);
        return outputDateTimeString;
    }

    public static Date addThreeMonthDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(2, 3);
        Date date = cal.getTime();
        return date;
    }
}