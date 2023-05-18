package com.log.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Author lsw
 * @Date 2023/5/6 13:19
 */
public class ExceptionUtil {

    public static String throwableToString(Throwable throwable) {
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw);) {
                throwable.printStackTrace(pw);
            }
            return sw.toString();
        }
        return null;
    }

}
