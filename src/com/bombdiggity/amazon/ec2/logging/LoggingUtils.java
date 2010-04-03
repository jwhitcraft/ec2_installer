
package com.bombdiggity.amazon.ec2.logging;

import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

public class LoggingUtils
{

    public LoggingUtils()
    {
    }

    public static void initConsoleLogging()
    {
        Properties props = new Properties();
        props.setProperty("log4j.rootLogger", "INFO, console, lfile");
        props.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.console.layout.ConversionPattern", "%m%n");
        props.setProperty("log4j.appender.lfile", "org.apache.log4j.DailyRollingFileAppender");
        props.setProperty("log4j.appender.lfile.DatePattern", "'.'yyyy-MM-dd");
        props.setProperty("log4j.appender.lfile.File", "/usr/local/IMSServer/logs/imsset_startup.log");
        props.setProperty("log4j.appender.lfile.layout", "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.lfile.layout.ConversionPattern", "%-4r [%t] %-5p %c %x - %m%n");
        PropertyConfigurator.configure(props);
    }
}