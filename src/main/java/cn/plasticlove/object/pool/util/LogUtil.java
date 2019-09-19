package cn.plasticlove.object.pool.util;

import java.io.IOException;
import java.util.Date;

import java.util.logging.*;

/**
 * @author luka-seu
 * 打印日志工具
 **/

public class LogUtil {


    public static void info(String tag, String msg) throws IOException {
        Logger log = Logger.getLogger("pool log");
        log.setLevel(Level.ALL);
        FileHandler fileHandler = new FileHandler("log.log");
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new LogFormatter());
        log.addHandler(fileHandler);
        log.info("This is test java util log");
    }

    static class LogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            Date date = new Date();
            String sDate = date.toString();
            return "[" + sDate + "]" + "[" + record.getLevel() + "]"
                    + record.getClass() + record.getMessage() + "\n";
        }
    }


}
