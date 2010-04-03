
package com.bombdiggity.amazon.ec2.install;

import com.bombdiggity.amazon.ec2.logging.LoggingUtils;
import com.bombdiggity.util.HTTPUtils;
import com.bombdiggity.util.ZipUtils;

import java.io.File;
import org.apache.log4j.Logger;

// Referenced classes of package com.bombdiggity.amazon.ec2.install:
//            InstallSession, InstallParser, InstallCommands

public class Installer
{

    public Installer()
    {
    }

    public static void queryEnvironment(InstallSession session)
    {
        try
        {
            String urls[] = {
                "http://169.254.169.254/latest/meta-data/ami-id", "http://169.254.169.254/latest/meta-data/ami-launch-index", "http://169.254.169.254/latest/meta-data/ami-manifest-path", "http://169.254.169.254/latest/meta-data/instance-id", "http://169.254.169.254/latest/meta-data/instance-type", "http://169.254.169.254/latest/meta-data/hostname", "http://169.254.169.254/latest/meta-data/local-hostname", "http://169.254.169.254/latest/meta-data/local-ipv4", "http://169.254.169.254/latest/meta-data/public-hostname", "http://169.254.169.254/latest/meta-data/public-ipv4", 
                "http://169.254.169.254/latest/meta-data/reservation-id", "http://169.254.169.254/latest/meta-data/security-groups", "http://169.254.169.254/latest/meta-data/product-codes"
            };
            session.setEnvironmentValue((new StringBuilder("AWSEC2_METADATA_")).append("instance-type".replace("-", "_").toUpperCase()).toString(), "m1.small");
            for(int i = 0; i < urls.length; i++)
            {
                String url = urls[i];
                byte data[] = HTTPUtils.HTTPRequestToByteArray(url, "GET", null, null);
                if(data != null)
                {
                    String urlParts[] = url.split("/");
                    if(urlParts.length > 1)
                        try
                        {
                            String value = new String(data, "UTF-8");
                            session.setEnvironmentValue((new StringBuilder("AWSEC2_METADATA_")).append(urlParts[urlParts.length - 1].replace("-", "_").toUpperCase()).toString(), value);
                        }
                        catch(Exception e)
                        {
                            Logger.getLogger(com.bombdiggity.amazon.ec2.install.Installer.class).error((new StringBuilder("Installer.queryEnvironment: conversion to string: ")).append(e.toString()).toString());
                        }
                }
            }

        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.Installer.class).error((new StringBuilder("Installer.queryEnvironment: ")).append(e.toString()).toString());
        }
    }

    public static void install(InstallSession session)
    {
        try
        {
            File file = session.getBaseFolder();
            File startFile = new File((new StringBuilder(String.valueOf(file.getPath()))).append("/").append("startup.xml").toString());
            if(!startFile.exists())
            {
                Logger.getLogger(com.bombdiggity.amazon.ec2.install.Installer.class).error("Installer.install: startup.properties file is missing");
            } else
            {
                InstallParser parser = new InstallParser(session);
                parser.parseStartup(startFile);
            }
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.Installer.class).error((new StringBuilder("Installer.install: ")).append(e.toString()).toString());
        }
    }

    public static void main(String argv[])
        throws Exception
    {
        LoggingUtils.initConsoleLogging();
        try
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info("Installer.doAmazonInstall");
            String destPath = "/opt";
            File tmpFile = new File(destPath);
            if(!tmpFile.exists())
            {
                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("Installer.doAmazonInstall: destination base missing: ")).append(destPath).toString());
            } else
            {
                destPath = (new StringBuilder(String.valueOf(destPath))).append("/working").toString();
                tmpFile = new File(destPath);
                if(!tmpFile.exists())
                    try
                    {
                        tmpFile.mkdir();
                    }
                    catch(Exception exception) { }
                File startupFile = null;
                String rightScaleStartup = "/opt/rightscale/wowza/startup.zip";
                startupFile = new File(rightScaleStartup);
                if(startupFile.exists())
                {
                    Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  rightScaleStartup: ")).append(startupFile).toString());
                } else
                {
                    String userDataUrl = "http://169.254.169.254/2007-08-29/user-data";
                    String startupPath = (new StringBuilder(String.valueOf(destPath))).append("/user-data.zip").toString();
                    startupFile = new File(startupPath);
                    if(startupFile.exists())
                    {
                        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  startupFile: ")).append(startupFile).toString());
                    } else
                    {
                        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  url: ")).append(userDataUrl).toString());
                        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  destination: ")).append(startupFile).toString());
                        boolean success = HTTPUtils.HTTPRequestToFile(startupFile, userDataUrl, "get", null, null);
                        if(!success)
                        {
                            String defaultUrl = "http://www.imscdn.com/instance/default.zip";
                            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder(" *loaddefaultconfig: ")).append(defaultUrl).toString());
                            success = HTTPUtils.HTTPRequestToFile(startupFile, defaultUrl, "get", null, null);
                            if(!success)
                                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error("Installer.doAmazonInstall: missing configuration");
                        }
                    }
                }
                if(startupFile == null)
                    Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error("Installer.doAmazonInstall: package is NULL");
                else
                if(!startupFile.exists())
                {
                    Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("Installer.doAmazonInstall: package is missing: ")).append(startupFile).toString());
                } else
                {
                    Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  unzip: ")).append(startupFile).toString());
                    File resultFolder = ZipUtils.unzipFile(startupFile, new File(destPath));
                    if(resultFolder == null)
                    {
                        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("Installer.doAmazonInstall: package could not be unzipped: ")).append(startupFile).toString());
                    } else
                    {
                        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  result: ")).append(resultFolder).toString());
                        InstallSession session = new InstallSession(resultFolder);
                        queryEnvironment(session);
                        install(session);
                    }
                }
            }
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.Installer.class).error((new StringBuilder("Installer.doAmazonInstall: ")).append(e.toString()).toString());
        }
    }

    public static final String STARTUP_FILENAME = "startup.xml";
}