package cn.plasticlove.object.pool.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import java.util.logging.*;

/**
 * @author luka-seu
 * 打印日志工具
 **/

public class LogUtil {
    private static FileHandler fileHandler;

    static {
        try {
            fileHandler = new FileHandler("log.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void info(String tag, String msg) {
        Logger log = Logger.getLogger("pool log");
        log.setLevel(Level.ALL);
        try {
            fileHandler.setEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new LogFormatter());
        log.addHandler(fileHandler);
        log.info(tag + " " + msg);
    }

    static class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            Date date = new Date();
            String sDate = date.toString();
            return "[" + sDate + "]" + "[" + record.getThreadID() + "]" + "[" + record.getLevel() + "]"
                    + record.getClass() + record.getMessage() + "\n";

        }
    }


}
