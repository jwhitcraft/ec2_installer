
package com.bombdiggity.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import org.apache.log4j.Logger;

public class HTTPUtils
{

    public HTTPUtils()
    {
    }

    public static boolean HTTPRequestToFile(File file, String inUrl, String method, String data, List headers)
    {
        boolean success = false;
        try
        {
            if(file.exists())
                file.delete();
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.HTTPUtils.class).error((new StringBuilder("HTTPUtils.HTTPRequestToFile delete (")).append(file).append("): ").append(e.toString()).toString());
        }
        try
        {
            DataOutputStream printout = null;
            DataInputStream input = null;
            URL url = new URL(inUrl);
            URLConnection urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            if(headers != null)
            {
                for(Iterator iter = headers.iterator(); iter.hasNext();)
                {
                    Map nameValuePair = (Map)iter.next();
                    String key;
                    String value;
                    for(Iterator iter2 = nameValuePair.keySet().iterator(); iter2.hasNext(); urlConn.setRequestProperty(key, value))
                    {
                        key = (String)iter2.next();
                        value = (String)nameValuePair.get(key);
                    }

                }

            }
            if(data != null)
            {
                byte inData[] = data.getBytes("UTF-8");
                printout = new DataOutputStream(urlConn.getOutputStream());
                printout.write(inData);
                printout.flush();
                printout.close();
                printout = null;
            }
            input = new DataInputStream(urlConn.getInputStream());
            DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(file, false));
            int rChunk = 0x10000;
            byte myData[] = new byte[rChunk];
            do
            {
                int bytesRead = input.read(myData, 0, rChunk);
                if(bytesRead == -1)
                    break;
                dataOut.write(myData, 0, bytesRead);
                Thread.sleep(1L);
            } while(true);
            input.close();
            input = null;
            success = true;
        }
        catch(Exception exception) { }
        return success;
    }

    public static byte[] HTTPRequestToByteArray(String inUrl, String method, String data, List headers)
    {
        byte ret[] = (byte[])null;
        ByteArrayOutputStream byteOut = null;
        try
        {
            DataOutputStream printout = null;
            DataInputStream input = null;
            URL url = new URL(inUrl);
            URLConnection urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            if(headers != null)
            {
                for(Iterator iter = headers.iterator(); iter.hasNext();)
                {
                    Map nameValuePair = (Map)iter.next();
                    String key;
                    String value;
                    for(Iterator iter2 = nameValuePair.keySet().iterator(); iter2.hasNext(); urlConn.setRequestProperty(key, value))
                    {
                        key = (String)iter2.next();
                        value = (String)nameValuePair.get(key);
                    }

                }

            }
            if(data != null)
            {
                byte inData[] = data.getBytes("UTF-8");
                printout = new DataOutputStream(urlConn.getOutputStream());
                printout.write(inData);
                printout.flush();
                printout.close();
                printout = null;
            }
            input = new DataInputStream(urlConn.getInputStream());
            byteOut = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(byteOut);
            int rChunk = 0x10000;
            byte myData[] = new byte[rChunk];
            do
            {
                int bytesRead = input.read(myData, 0, rChunk);
                if(bytesRead == -1)
                    break;
                dataOut.write(myData, 0, bytesRead);
                Thread.sleep(1L);
            } while(true);
            input.close();
            input = null;
            ret = byteOut.toByteArray();
        }
        catch(Exception exception) { }
        return ret;
    }
}