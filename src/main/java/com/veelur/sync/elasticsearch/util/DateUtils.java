package com.veelur.sync.elasticsearch.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author: veelur
 * @date: 18-9-25
 * @Description: {相关描述}
 */
public class DateUtils {

    private final static Logger logger = LoggerFactory.getLogger(DateUtils.class);
    private final static String DATE_YYYY_MM_DD = "yyyy-MM-dd";

    private final static String DATE_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private final static String DATE_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

//    private final static String DATE_YYYY_MM_DD_HH_MM_SS

    public static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_YYYY_MM_DD_HH_MM_SS);
        String current = dateFormat.format(new Date());
        return current;
    }

    public static String getDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_YYYY_MM_DD_HH_MM_SS);
        String current = dateFormat.format(date);
        return current;
    }

    public static String getDateTime2(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_YYYYMMDDHHMMSS);
        String current = dateFormat.format(date);
        return current;
    }

    public static String getDateTime3(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_YYYY_MM_DD);
        return dateFormat.format(date);
    }

    public static Date convertDate(String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        if (date.length() <= 10) {
            return strToDate3(date);
        } else {
            return strToDate(date);
        }
    }

    /**
     * 将日期字符串按照指定格式转换成Date类型
     *
     * @param date   String
     * @param format String
     * @return Date
     */
    public static Date strToDate(String date, String format) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        if (StringUtils.isBlank(format)) {
            format = DATE_YYYY_MM_DD;
        }

        Date result = null;
        try {
            result = new SimpleDateFormat(format).parse(date);
        } catch (ParseException e) {
            //logger.error("时间格式不正确，date=" + date + ", format=" + format + "，失败原因=" + e.getMessage() + ".", e);
        }
        return result;
    }

    /**
     * 将日期字符串按照指定格式转换成Date类型,为GMT格式
     *
     * @param date   String
     * @param format String
     * @return Date
     */
    public static Date strToDate4GMT(String date, String format) {
        if (StringUtils.isBlank(date)) {
            return null;
        }
        if (StringUtils.isBlank(format)) {
            format = DATE_YYYY_MM_DD_HH_MM_SS;
        }

        Date result = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            result = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            logger.error("时间格式不正确，date=" + date + ", format=" + format + "，失败原因=" + e.getMessage() + ".", e);
        }
        return result;
    }

    public static Date strToDate4GMT(String date) {
        return strToDate4GMT(date, DATE_YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 将日期字符串按照指定格式转换成Date类型
     *
     * @param date
     * @return
     */
    public static Date strToDate2(String date) {
        return strToDate(date, DATE_YYYYMMDDHHMMSS);
    }

    public static Date strToDate3(String date) {
        return strToDate(date, DATE_YYYY_MM_DD);
    }

    /**
     * 将日期字符串按照指定格式转换成Date类型
     *
     * @param date String
     * @return Date
     */
    public static Date strToDate(String date) {
        return strToDate(date, DATE_YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 将日期类型按照指定格式转换成String
     *
     * @param date
     * @param format
     * @return
     */
    public static String getDateStr(Date date, String format) {
        if (date == null) {
            return "";
        }
        if (StringUtils.isBlank(format)) {
            format = DATE_YYYY_MM_DD_HH_MM_SS;
        }
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 在指定日期的基础上添加天数，返回添加后的日期
     *
     * @param date
     * @param days
     * @return
     */
    public static Date addDays(Date date, int days) {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.setTime(date);
        todayEnd.add(Calendar.DATE, days);
        return todayEnd.getTime();
    }

    /**
     * 判断time时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @param time      当前时间
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public static boolean isEffectiveDate(Date time, Date startTime, Date endTime) {

        if (time == null || startTime == null || endTime == null) {
            return false;

        }
        if (time.after(startTime) && time.before(endTime)) {
            return true;
        }
        return (time.getTime() == startTime.getTime() || time.getTime() == endTime.getTime());
    }

    /**
     * 日期加一个分钟后的日期时间戳
     *
     * @param dateString
     * @param minute
     * @return
     */
    public static Long addDateMinute(String dateString, int minute) {
        if (StringUtils.isBlank(dateString)) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(DATE_YYYY_MM_DD_HH_MM_SS);// 24小时制
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (date == null)
            return null;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MINUTE, minute);
        date = c.getTime();
        return date.getTime();
    }


    public static void main(String[] args) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date()));

        String dateStr = simpleDateFormat.format(new Date()).replace(" ", "T") + "+08:00";
        System.out.println(dateStr);
    }

}
