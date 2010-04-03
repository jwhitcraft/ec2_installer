
package com.bombdiggity.util;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.log4j.Logger;

public class ZipUtils
{

    public ZipUtils()
    {
    }

    public static File unzipFile(File srcFile, File dstFolder)
    {
        if(!srcFile.exists())
        {
            Logger.getLogger(com.bombdiggity.util.ZipUtils.class).error((new StringBuilder("ZipUtils.unzipFile: srcFile missing: ")).append(srcFile).toString());
            return null;
        }
        if(!dstFolder.exists())
        {
            Logger.getLogger(com.bombdiggity.util.ZipUtils.class).error((new StringBuilder("ZipUtils.unzipFile: dstFolder missing: ")).append(dstFolder).toString());
            return null;
        }
        if(!dstFolder.isDirectory())
        {
            Logger.getLogger(com.bombdiggity.util.ZipUtils.class).error((new StringBuilder("ZipUtils.unzipFile: dstFolder is not a folder: ")).append(dstFolder).toString());
            return null;
        }
        File ret = dstFolder;
        boolean allSameBase = true;
        String dirBase = null;
        try
        {
            ZipFile zf = new ZipFile(srcFile);
            Enumeration zipEnum = zf.entries();
            String dir = dstFolder.getPath().replace("\\", "/");
            while(zipEnum.hasMoreElements()) 
            {
                ZipEntry item = (ZipEntry)zipEnum.nextElement();
                String itemDir = item.getName().replace("\\", "/");
                String dirs[] = itemDir.split("[/]");
                if(dirs.length > 0)
                    if(dirBase == null)
                        dirBase = dirs[0];
                    else
                    if(!dirs[0].equals(dirBase))
                        allSameBase = false;
                String allDirs = dir;
                for(int i = 0; i < (item.isDirectory() ? dirs.length : dirs.length - 1); i++)
                {
                    allDirs = (new StringBuilder(String.valueOf(allDirs))).append("/").append(dirs[i]).toString();
                    File newdir = new File(allDirs);
                    if(!newdir.exists())
                        try
                        {
                            newdir.mkdir();
                        }
                        catch(Exception e)
                        {
                            Logger.getLogger(com.bombdiggity.util.ZipUtils.class).error((new StringBuilder("ZipUtils.unzipFile.mkdir (")).append(allDirs).append("): ").append(e.toString()).toString());
                        }
                }

                if(!item.isDirectory())
                {
                    String newfile = (new StringBuilder(String.valueOf(dir))).append("/").append(item.getName().replace("\\", "/")).toString();
                    try
                    {
                        InputStream is = zf.getInputStream(item);
                        FileOutputStream fos = new FileOutputStream(newfile);
                        int ch;
                        while((ch = is.read()) != -1) 
                            fos.write(ch);
                        is.close();
                        fos.close();
                    }
                    catch(Exception e)
                    {
                        Logger.getLogger(com.bombdiggity.util.ZipUtils.class).error((new StringBuilder("ZipUtils.unzipFile.writeFile (")).append(newfile).append("): ").append(e.toString()).toString());
                    }
                }
            }
            zf.close();
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.ZipUtils.class).error((new StringBuilder("ZipUtils.unzipFile: ")).append(e.toString()).toString());
        }
        if(allSameBase && dirBase != null)
            ret = new File((new StringBuilder(String.valueOf(dstFolder.getPath()))).append("/").append(dirBase).toString());
        return ret;
    }
}