package com.lqsmart.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/22.
 */
public class LqUtil {
    public static final String C_TIME_PATTON_DEFAULT = "yyyy-MM-dd HH:mm:ss";
    public static final String C_DATE_PATTON_DEFAULT = "yyyy-MM-dd";
    public static final String C_DATA_PATTON_YYYYMMDD = "yyyyMMdd HH:mm:ss";

    public static String trimToNull(Object attribute) {
        if (attribute == null) {
            return null;
        }
        String str = attribute.toString();
        if (isEmpty(str)) {
            return null;
        }
        return str;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断某个类是否是指定接口的实现类
     * @param cls
     * @param interFaceClss
     * @return
     */
    public static boolean isInterFace(Class cls,Class interFaceClss){
        Class[] classes = cls.getInterfaces();
        for(Class c:classes){
            if(c == interFaceClss){
                return true;
            }
        }

        return false;
    }

    /**
     * 二进制转字符串
     *
     * @param b
     * @return
     */
    public static String byte2hex(byte[] b) {
        try {
            return new String(b,"UTF-8");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 字符串转二进制
     *
     * @param str
     * @return
     */
    public static byte[] hex2byte(String str){
        try {
            return str.getBytes("UTF-8");
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    public static String getDateTime(Date date) {
        return DateToString(date,C_TIME_PATTON_DEFAULT);
    }

    public static String DateToString(Date date, String parttern) {
        String dateString = null;
        if (date != null) {
            try {
                dateString = new SimpleDateFormat(parttern).format(date);
            } catch (Exception e) {
            }
        }
        return dateString;
    }

    public static Date getDateTime(String date,String strFormat) {
        return parseDate(strFormat,date);
    }

    static Date parseDate(String strFormat, String dateValue) {
        if (dateValue == null) {
            return null;
        }

        if (strFormat == null) {
            strFormat = C_TIME_PATTON_DEFAULT;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(strFormat);
        Date newDate = null;

        try {
            newDate = dateFormat.parse(dateValue);
        } catch (ParseException pe) {
            pe.printStackTrace();
            newDate = null;
        }

        return newDate;
    }


    public static Properties loadProperty(String properties) {

        InputStream inputStream = null;
        Properties p = null;
        try {
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(properties);
            if(inputStream == null){
                throw new IllegalArgumentException("Properties file not found: "
                        + properties);
            }

            p = new Properties();
            p.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Properties file not found: "
                    + properties);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Properties file can not be loading: " + properties);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return p;
    }

    public static Properties createProperties(Map<String,String> map){
        if(map == null || map.isEmpty()){
            return null;
        }

        Properties properties = new Properties();

        if(map != null && !map.isEmpty()){
            for(Map.Entry<String,String> entry:map.entrySet()){
                properties.setProperty(entry.getKey(),entry.getValue());
            }
        }

        return properties;
    }

    public static Properties createProperties(Map<String,String> map, Map<String,String> globalMap){
        if((map == null || map.isEmpty()) && globalMap.isEmpty()){
            return null;
        }

        Properties properties = new Properties();

        if(map != null && !map.isEmpty()){
            for(Map.Entry<String,String> entry:map.entrySet()){
                properties.setProperty(entry.getKey(),entry.getValue());
            }
        }

        if(!globalMap.isEmpty()){
            for(Map.Entry<String,String> entry:globalMap.entrySet()){
                if(properties.containsKey(entry.getKey())){
                    continue;
                }
                properties.setProperty(entry.getKey(),entry.getValue());
            }
        }
        return properties;
    }

    /**
     * 替换 ? 占位符
     * @param sourceStr abc?sdfsd?sdfsd
     * @param values
     * @return
     */
    public static String format(String sourceStr,String... values){
        if(values == null || values.length==0){
            return sourceStr;
        }

        int valueLength = 0;
        for(String str:values){
            valueLength+=str.length()-1;
        }
        valueLength = sourceStr.length()+ valueLength;

        char[] newChars = new char[valueLength];
        int offset = -1;
        int num = -1;
        char c;
        int i = 0;
        String tmp;
        final int oldStrLength = sourceStr.length()-1;
        final int valueArrayLength = values.length-1;
        while (i<valueLength){
            offset++;
            if(offset>oldStrLength){
                break;
            }
            c = sourceStr.charAt(offset);
            if(c!='?'){
                newChars[i++] = c;
                continue;
            }
            num++;
            if(num>valueArrayLength){
                newChars[i++] = c;
                continue;
            }
            tmp = values[num];
            for(int j=0,count=tmp.length();j<count;j++){
                newChars[i++] = tmp.charAt(j);
            }
        }
        return new String(newChars);
    }
}
