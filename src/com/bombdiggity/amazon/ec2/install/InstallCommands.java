
package com.bombdiggity.amazon.ec2.install;

import com.bombdiggity.util.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

// Referenced classes of package com.bombdiggity.amazon.ec2.install:
//            InstallSession

public class InstallCommands
{

    public InstallCommands()
    {
    }

    public static void installPackage(InstallSession session, String packageName)
    {
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info("InstallCommands.installPackage");
        String packagePath = packageName.trim();
        String packageParts[] = packagePath.split("[/]");
        if(packageParts.length <= 0)
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("InstallCommands.installPackage: package parts zero length: ")).append(packageParts).toString());
        } else
        {
            String destPath = "/opt";
            File destinationFile = new File(destPath);
            if(!destinationFile.exists())
            {
                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("InstallCommands.installPackage: destination base missing: ")).append(destinationFile).toString());
            } else
            {
                destPath = (new StringBuilder(String.valueOf(destPath))).append("/working").toString();
                destinationFile = new File(destPath);
                if(!destinationFile.exists())
                    try
                    {
                        destinationFile.mkdir();
                    }
                    catch(Exception exception) { }
                destPath = (new StringBuilder(String.valueOf(destPath))).append("/").append(packageParts[packageParts.length - 1]).toString();
                destinationFile = new File(destPath);
                String url = (new StringBuilder("http://wowzamediasystems.s3.amazonaws.com/")).append(packagePath).toString();
                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  package: ")).append(packagePath).toString());
                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  url: ")).append(url).toString());
                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  destination: ")).append(destinationFile).toString());
                boolean success = HTTPUtils.HTTPRequestToFile(destinationFile, url, "get", null, null);
                if(!success)
                {
                    Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("InstallCommands.installPackage: package not downloaded: ")).append(url).toString());
                } else
                {
                    Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  unzip: ")).append(destinationFile.getParent()).toString());
                    File resultFolder = ZipUtils.unzipFile(destinationFile, new File(destinationFile.getParent()));
                    if(resultFolder == null)
                        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("InstallCommands.installPackage: package could not be unzipped: ")).append(destinationFile).toString());
                    else
                        installFolder(session, resultFolder);
                }
            }
        }
    }

    public static void installFolder(InstallSession session, String folderPath)
    {
        folderPath = AmazonEC2Utils.fixRelativePath(session, folderPath);
        File folder = new File(folderPath);
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info("InstallCommands.installFolder");
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  folder: ")).append(folder).toString());
        installFolder(session, folder);
    }

    public static void installFolder(InstallSession session, File folder)
    {
        File targerFolder = new File("/usr/local/WowzaMediaServerPro");
        if(!targerFolder.exists())
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error("InstallCommands.installFolder: WowzaMediaServerPro folder not found: /usr/local/WowzaMediaServerPro");
        } else
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  mergeSrc: ")).append(folder).toString());
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  mergeTarget: ")).append(targerFolder).toString());
            FileUtils.mergeDirectories(folder, targerFolder);
        }
    }

    public static void downloadFile(InstallSession session, String url, String method, String data, List headers, String destination, String action)
    {
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info("InstallCommands.downloadFile");
        destination = AmazonEC2Utils.fixRelativePath(session, destination);
        File destinationFile = new File(destination);
        FileUtils.validatePath(destinationFile, false);
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  url: ")).append(url).toString());
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  destination: ")).append(destinationFile).toString());
        boolean success = HTTPUtils.HTTPRequestToFile(destinationFile, url, method, data, headers);
        if(success && action != null)
            performAction(session, destinationFile, action);
    }

    public static void fetchFile(InstallSession session, String awsAccessKeyId, String awsSecretAccessKey, String bucket, String key, String destination, String action)
    {
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info("InstallCommands.fetchFile");
        destination = AmazonEC2Utils.fixRelativePath(session, destination);
        File destinationFile = new File(destination);
        FileUtils.validatePath(destinationFile, false);
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  bucket: ")).append(bucket).toString());
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  key: ")).append(key).toString());
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  destination: ")).append(destinationFile).toString());
        boolean success = S3Utils.S3RequestToFile(destinationFile, awsAccessKeyId, awsSecretAccessKey, bucket, key);
        if(success && action != null)
            performAction(session, destinationFile, action);
    }

    private static void performAction(InstallSession session, File src, String action)
    {
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  action: ")).append(action).toString());
        if(action.equals("unzip"))
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  unzipTo: ")).append(src.getParent()).toString());
            File resultFolder = ZipUtils.unzipFile(src, new File(src.getParent()));
            if(resultFolder == null)
                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error("InstallCommands.performAction: action UNZIP: File could not be unzipped");
        } else
        if(action.equals("install"))
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  unzipTo: ")).append(src.getParent()).toString());
            File resultFolder = ZipUtils.unzipFile(src, new File(src.getParent()));
            if(resultFolder == null)
                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error("InstallCommands.performAction: action INSTALL: File could not be unzipped");
            else
                installFolder(session, resultFolder);
        } else
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("InstallCommands.performAction: action UNKNOWN: ")).append(action).toString());
        }
    }

    public static void runScript(InstallSession session, String script, List params)
    {
        File fixScriptFile;
        List cmd;
        Process proc;
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info("InstallCommands.runScript");
        script = AmazonEC2Utils.fixRelativePath(session, script);
        File scriptFile = new File(script);
        if(!scriptFile.exists())
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("InstallCommands.runScript: Script file not found: ")).append(scriptFile).toString());
        }
        fixScriptFile = FileUtils.fixLineFeeds(scriptFile);
        if(fixScriptFile == null)
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("InstallCommands.runScript: Error fixing script linefeeds: ")).append(scriptFile).toString());
        }
        cmd = new ArrayList();
        String scriptStr = fixScriptFile.getAbsolutePath();
        cmd.add(scriptStr);
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  script: ")).append(scriptFile.getAbsolutePath()).toString());
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  temp(fix-linefeeds): ")).append(fixScriptFile.getAbsolutePath()).toString());
        if(params != null)
        {
            for(Iterator iter = params.iterator(); iter.hasNext(); cmd.add(scriptStr))
            {
                String param = (String)iter.next();
                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  param: ")).append(param).toString());
            }

        }
        proc = null;
        try
        {
            ProcessBuilder pb = new ProcessBuilder(new String[] {
                "chmod", "+x", fixScriptFile.getAbsolutePath()
            });
            proc = pb.start();
            int exitVal = proc.waitFor();
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("InstallCommands.runScript: ")).append(e.toString()).toString());
        }
        if(proc != null)
        {
            close(proc.getOutputStream());
            close(proc.getInputStream());
            close(proc.getErrorStream());
            proc.destroy();
        }
        try
        {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(session.getBaseFolder());
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  workingDir: ")).append(session.getBaseFolder()).toString());
            Map env = session.getEnvironmentMap();
            String key;
            String value;
            for(Iterator iter = env.keySet().iterator(); iter.hasNext(); Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info((new StringBuilder("  env: ")).append(key).append("=").append(value).toString()))
            {
                key = (String)iter.next();
                value = (String)env.get(key);
                pb.environment().put(key, value);
            }

            proc = pb.start();
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info("***** script output start *****");
            BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            for(String line = stdout.readLine(); line != null; line = stdout.readLine())
            {
                line = line.trim();
                if(line.length() > 0)
                    Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info(line);
            }

            boolean hitError = false;
            BufferedReader stderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            for(String line = stderr.readLine(); line != null; line = stderr.readLine())
            {
                if(!hitError)
                {
                    Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info("%%%%% script errors %%%%%");
                    hitError = true;
                }
                line = line.trim();
                if(line.length() > 0)
                    Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error(line);
            }

            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).info("***** script output stop *****");
            int exitVal = proc.waitFor();
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallCommands.class).error((new StringBuilder("InstallCommands.runScript: ")).append(e.toString()).toString());
        }
        if(proc != null)
        {
            close(proc.getOutputStream());
            close(proc.getInputStream());
            close(proc.getErrorStream());
            proc.destroy();
        }
        fixScriptFile.deleteOnExit();
    }

    private static void close(Closeable c)
    {
        if(c != null)
            try
            {
                c.close();
            }
            catch(IOException ioexception) { }
    }

    public static final String DOWNLOAD_ACTION_UNZIP = "unzip";
    public static final String DOWNLOAD_ACTION_INSTALL = "install";
    public static final String DOWNLOAD_METHOD_POST = "post";
    public static final String DOWNLOAD_METHOD_GET = "get";
    public static final String PACKAGE_BASEURL = "http://wowzamediasystems.s3.amazonaws.com";
    public static final String PACKAGE_DESTINATION_FOLDER = "working";
    public static final String PACKAGE_DESTINATION_BASE = "/opt";
    public static final String TARGET_WOWZAMEDIASERVERPRO = "/usr/local/WowzaMediaServerPro";
    public static final String STARTUP_DEFAULT_PACKAGE = "com/wowza/startup/default.zip";
}