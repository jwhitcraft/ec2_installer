
package com.bombdiggity.util;

import com.bombdiggity.amazon.ec2.install.InstallSession;

import org.apache.log4j.Logger;

public class AmazonEC2Utils
{

    public AmazonEC2Utils()
    {
    }

    public static String fixRelativePath(InstallSession session, String path)
    {
        path = path.replace("\\", "/").trim();
        if(path.length() <= 0)
        {
            Logger.getLogger(com.bombdiggity.util.AmazonEC2Utils.class).error("InstallCommands.fixRelativePath: path is empty");
        } else
        {
            String firstChar = path.substring(0, 1);
            String secondChar = path.length() < 2 ? "" : path.substring(1, 2);
            if(!firstChar.equals("/") && !secondChar.equals(":"))
                path = (new StringBuilder(String.valueOf(session.getBaseFolder().getAbsolutePath()))).append("/").append(path).toString();
        }
        return path;
    }
}