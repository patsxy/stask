package com.sy.cc.comm.util;



import org.apache.logging.log4j.core.util.CronExpression;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CronUtil {

    static final String xin="*";
    static final String wenhao="?";
    static final String dao="-";
    static final String mei="/";
    static final String huo=",";
    public static List<String> descCorn2(String cronExpression) {
        System.out.println("接收到的cron表达式信息"+cronExpression);
        List<String> result = new ArrayList<String>();
        if (cronExpression == null || cronExpression.length() < 1) {
            return result;
        } else {
            CronExpression exp = null;
            Calendar calendar = Calendar.getInstance();
            String cronDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE);
            String sStart = cronDate + " 00:00:00";
            System.out.println("sStart信息"+sStart);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dStart = null;
            Date dEnd = null;
            try {
                exp = new CronExpression(cronExpression);
                dStart = sdf.parse(sStart);
                calendar.setTime(dStart);
                calendar.add(Calendar.DATE, 1);
                dEnd = calendar.getTime();
                System.out.println("dEnd信息"+dEnd);
            } catch ( ParseException e) {
                e.printStackTrace();
                return result;
            }
            Date dd = new Date();
            dd = exp.getNextValidTimeAfter(dd);
            System.out.println("dd信息"+dd);
            while (dd.getTime() < dEnd.getTime()) {
                result.add(sdf.format(dd));
                dd = exp.getNextValidTimeAfter(dd);
            }
            exp = null;
        }
        return result;


    }
    public static String descCorn(String cronExp) {
        String[] tmpCorns = cronExp.split(" ");
        StringBuffer sBuffer = new StringBuffer();
       /* if (tmpCorns.length != 6) {
        	return  ("表达式位数超过6位，请重新选择");
           // throw new RuntimeException("请补全表达式,必须标准的cron表达式才能解析");
        }*/
        try {
            if (tmpCorns.length == 7) {
                // 解析月
                descYear(tmpCorns[6], sBuffer);
            }
        // 解析月
        descMonth(tmpCorns[4], sBuffer);
        // 解析周
        descWeek(tmpCorns[5], sBuffer);

        // 解析日
        descDay(tmpCorns[3], sBuffer);

        // 解析时
        descHour(tmpCorns[2], sBuffer);

        // 解析分
        descMintue(tmpCorns[1], sBuffer);

        // 解析秒
        descSecond(tmpCorns[0], sBuffer);
        sBuffer.append(" 执行");
        }catch(Exception e) {
            e.printStackTrace();
        	return "表达式解析错误,请检查或重新选择";
        }
        return sBuffer.toString();

    }

    /**
     * 描述
     
     * @param sBuffer
     * @author Norton Lai
     * @created 2019-2-27 下午5:01:09
     */
    private static void descSecond(String s, StringBuffer sBuffer) {
        String danwei="秒";
        desc(s, sBuffer, danwei);
    }

    /**
     * 描述
     * @param s
     * @param sBuffer
     * @param danwei
     * @author Norton Lai
     * @created 2019-2-27 下午5:16:19
     */
    private static void desc(String s, StringBuffer sBuffer, String danwei) {
        if (s.equals("1/1")) {
            s="*";
        }
        if (s.equals("0/0")) {
            s="0";
        }
        if (xin.equals(s)) {
            sBuffer.append("每"+danwei);
            return;
        }
        if (wenhao.equals(s)) {
            return ;
        }
        if (s.contains(huo)) {
            String[] arr = s.split(huo);
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].length()!=0) {
                    sBuffer.append("第"+arr[i]+danwei+"和");
                }
            }
            sBuffer.deleteCharAt(sBuffer.length()-1);
            sBuffer.append("的");
            return;
        }
        if (s.contains(dao)) {
            String[] arr = s.split(dao);
            if (arr.length!=2) {
                throw new RuntimeException("表达式错误"+s);
            }
            sBuffer.append("从第"+arr[0]+danwei+"到第"+arr[1]+danwei+"每"+danwei);
            sBuffer.append("的");
            return;
        }
       
        if (s.contains(mei)) {
            String[] arr = s.split(mei);
            if (arr.length!=2) {
                throw new RuntimeException("表达式错误"+s);
            }
            if (arr[0].equals(arr[1])||arr[0].equals("0")) {
                sBuffer.append("每"+arr[1]+danwei);
            }else {
                sBuffer.append("每"+arr[1]+danwei+"的第"+arr[0]+danwei);
            }
            return;
        }
        sBuffer.append("第"+s+danwei);
    }

    /**
     * 描述
     
     * @param sBuffer
     * @author Norton Lai
     * @created 2019-2-27 下午5:01:00
     */
    private static void descMintue(String s, StringBuffer sBuffer) {
        desc(s, sBuffer, "分钟");
    }

    /**
     * 描述
     
     * @param sBuffer
     * @author Norton Lai
     * @created 2019-2-27 下午5:00:50
     */
    private static void descHour(String s, StringBuffer sBuffer) {
        desc(s, sBuffer, "小时");
    }

    /**
     * 描述
     
     * @param sBuffer
     * @author Norton Lai
     * @created 2019-2-27 下午5:00:39
     */
    private static void descDay(String s, StringBuffer sBuffer) {
        desc(s, sBuffer, "天");
    }

    /**
     * 描述
     
     * @param sBuffer
     * @author Norton Lai
     * @created 2019-2-27 下午5:00:30
     */
    private static void descWeek(String s, StringBuffer sBuffer) {
        //不解释 太麻烦
    }
//    private static String turnWeek(String week){
//        switch (week) {
//        case "1":
//            return"星期天";
//        case "2":
//            return"星期一";
//        case "3":
//            return"星期二";
//        case "4":
//            return"星期三";
//        case "5":
//            return"星期四";
//        case "6":
//            return"星期五";
//        case "7":
//            return"星期六";
//        default:
//            return null;
//        }
//    }

    /**
     * 描述
     
     * @param sBuffer
     * @author Norton Lai
     * @created 2019-2-27 下午5:00:15
     */
    private static void descMonth(String s, StringBuffer sBuffer) {
        desc(s, sBuffer, "月");
    }
    private static void descYear(String s, StringBuffer sBuffer) {
        desc(s, sBuffer, "年");
    }
    // 测试方法
    public static void main2(String[] args) {
//        String CRON_EXPRESSION = "0 0 0/2 * * ?";
        String CRON_EXPRESSION = "* 5-8 1,5,7 * * ? 2099";
//        String CRON_EXPRESSION = "0 0 5-8 * * ?";
        String[] fields = CRON_EXPRESSION.split(" ");
        if (fields.length==7){
            StringBuilder sb=new StringBuilder("");
           for(int i=0;i<6;i++){
               if(i==5){
                   sb.append(fields[i]);
               }else{
                   sb.append(fields[i]+" ");
               }


           }
            CRON_EXPRESSION=sb.toString();
        }
        System.out.println(descCorn(CRON_EXPRESSION));
    }
}
