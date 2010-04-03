
package com.bombdiggity.util;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;

// Referenced classes of package com.bombdiggity.util:
//            IFileProcess

public class FileUtils
{

    public FileUtils()
    {
    }

    public static void copyFile(File fromFile, File toFile)
    {
        try
        {
            FileChannel in = (new FileInputStream(fromFile)).getChannel();
            FileChannel out = (new FileOutputStream(toFile)).getChannel();
            in.transferTo(0L, in.size(), out);
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.FileUtils.class).error((new StringBuilder("copyFile: ")).append(e.toString()).toString());
        }
    }

    public static void validatePath(File filePath, boolean isfolder)
    {
        boolean isFirstOne = true;
        ArrayList pathParts = new ArrayList();
        String parent;
        for(; !filePath.exists(); filePath = new File(parent))
        {
            parent = filePath.getParent();
            if(parent == null)
                break;
            String pathPart = filePath.toString().substring(parent.length(), filePath.toString().length());
            if(!isfolder && isFirstOne)
                isFirstOne = false;
            else
                pathParts.add(0, pathPart);
        }

        String basePath = filePath.getAbsolutePath();
        Iterator iter = pathParts.iterator();
        while(iter.hasNext()) 
        {
            String pathPart = (String)iter.next();
            basePath = (new StringBuilder(String.valueOf(basePath))).append(pathPart).toString();
            File folder = new File(basePath);
            try
            {
                folder.mkdir();
                continue;
            }
            catch(Exception e)
            {
                Logger.getLogger(com.bombdiggity.util.FileUtils.class).error((new StringBuilder("FileUtils.validatePath: mkdir (")).append(folder).append("): ").append(e.toString()).toString());
            }
            break;
        }
    }

    public static void traverseDirectory(File dir, IFileProcess fileNotify)
    {
        try
        {
            fileNotify.onFile(dir);
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.FileUtils.class).error((new StringBuilder("FileUtils.traverseDirectory: notify: ")).append(e.toString()).toString());
        }
        if(dir.isDirectory())
        {
            String children[] = dir.list();
            for(int i = 0; i < children.length; i++)
                try
                {
                    traverseDirectory(new File(dir, children[i]), fileNotify);
                }
                catch(Exception e)
                {
                    Logger.getLogger(com.bombdiggity.util.FileUtils.class).error((new StringBuilder("FileUtils.traverseDirectory: recurse: ")).append(e.toString()).toString());
                }

        }
    }

    public static void mergeDirectories(File fromDir, File toDir)
    {
        String baseFromPath = fromDir.getAbsolutePath();
        String baseToPath = toDir.getAbsolutePath();
        class _cls1FileProcessor
            implements IFileProcess
        {

            public void onFile(File file)
            {
                String from = file.getAbsolutePath();
                String inc = from.substring(baseFromPath.length());
                String to = (new StringBuilder(String.valueOf(baseToPath))).append(inc).toString();
                try
                {
                    if(file.isDirectory())
                    {
                        File toFolder = new File(to);
                        if(!toFolder.exists())
                            toFolder.mkdir();
                        Logger.getLogger(com.bombdiggity.util.FileUtils.class).debug((new StringBuilder("mkdir ")).append(toFolder).toString());
                    } else
                    {
                        File toFile = new File(to);
                        if(toFile.exists())
                            toFile.delete();
                        FileUtils.copyFile(file, toFile);
                        Logger.getLogger(com.bombdiggity.util.FileUtils.class).debug((new StringBuilder("cp ")).append(file).append(" ").append(toFile).toString());
                    }
                }
                catch(Exception e)
                {
                    Logger.getLogger(com.bombdiggity.util.FileUtils.class).error((new StringBuilder("FileUtils.mergeDirectories: (cp ")).append(from).append(" ").append(to).append(")").append(e.toString()).toString());
                }
            }

            private String baseFromPath;
            private String baseToPath;

            public _cls1FileProcessor(String baseFromPath, String baseToPath)
            {
                this.baseFromPath = null;
                this.baseToPath = null;
                this.baseFromPath = baseFromPath;
                this.baseToPath = baseToPath;
            }
        }

        _cls1FileProcessor processor = new _cls1FileProcessor(baseFromPath, baseToPath);
        traverseDirectory(fromDir, processor);
    }

    public static File fixLineFeeds(File file)
    {
        File tmpFile = null;
        try
        {
            tmpFile = File.createTempFile("amzwow", ".tmp");
            PrintWriter out = new PrintWriter(tmpFile);
            BufferedReader in = new BufferedReader(new FileReader(file));
            for(String line = null; (line = in.readLine()) != null;)
                out.println(line);

            in.close();
            out.close();
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.FileUtils.class).error((new StringBuilder("FileUtils.fixLineFeeds: (")).append(file).append(")").append(e.toString()).toString());
        }
        return tmpFile;
    }
}