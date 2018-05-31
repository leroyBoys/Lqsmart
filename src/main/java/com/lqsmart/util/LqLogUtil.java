package com.lqsmart.util;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by leroy:656515489@qq.com
 * 2018/5/22.
 */
public class LqLogUtil {
    private final static LinkedHashMap<String,Long> timeMap = new LinkedHashMap<>(50);

    public static void outTime(String flag,String str){
        try {
            flag = flag == null?"_":flag;
            final Long lastTime = timeMap.get(flag);
            final long curTime = System.currentTimeMillis();
            if(lastTime == null){
                System.out.println(LqUtil.getDateTime(new Date())+":"+flag+":"+str);
            }else {
                System.out.println(LqUtil.getDateTime(new Date())+":"+flag+":"+str+":"+(curTime-lastTime)+" ms");
            }

            timeMap.put(flag,curTime);

            if(timeMap.size() >= 50){
                timeMap.remove(timeMap.keySet().iterator().next());
            }
        }catch (Exception e){
        }
    }

    public static String getException(Exception e) {
        StringBuilder bs = new StringBuilder();
        StackTraceElement[] a = e.getStackTrace();
        bs.append("\n Message: ").append(e.fillInStackTrace()).append("");
        for (int i = 0; i < a.length; i++) {
            bs.append("\n ").append(a[i].getClassName()).append("(Line:").append(a[i].getLineNumber()).append(",Method:").append(a[i].getMethodName()).append(")");
        }
        return bs.toString();
    }

    public static void log(String info){
        System.out.println(info);
    }

    public static void info(String... info){
        for(int i = 0;i<info.length;i++){
            System.out.println(info[i]);
        }
    }

    public static void warn(String info){
        System.out.println(info);
    }

    public static void error(String info){
        System.err.println(info);
    }

    /*public static void error(Object... info){
        System.out.println(info);
    }*/

    public static void error(String info,Exception e){
        e.printStackTrace();
        System.out.println(info);
    }

    public static void error(Class info,Exception e){
        e.printStackTrace();
        //System.out.println(info);
    }
}
