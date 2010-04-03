
package com.bombdiggity.util;

import com.amazon.s3.*;
import java.io.*;

import org.apache.log4j.Logger;

// Referenced classes of package com.bombdiggity.util:
//            HTTPUtils

public class S3Utils
{

    public S3Utils()
    {
    }

    public static boolean S3RequestToFile(File file, String awsAccessKeyId, String awsSecretAccessKey, String bucket, String key)
    {
        boolean success = false;
        try
        {
            if(file.exists())
                file.delete();
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.HTTPUtils.class).error((new StringBuilder("S3Utils.S3RequestToFile delete (")).append(file).append("): ").append(e.toString()).toString());
        }
        try
        {
            boolean secure = true;
            String server = "s3.amazonaws.com";
            CallingFormat format = CallingFormat.getSubdomainCallingFormat();
            AWSAuthConnection conn = new AWSAuthConnection(awsAccessKeyId, awsSecretAccessKey, secure, server, format);
            GetResponse getResponse = conn.get(bucket, key, null);
            if(getResponse == null)
                Logger.getLogger(com.bombdiggity.util.HTTPUtils.class).error((new StringBuilder("S3Utils.S3RequestToFile: No response code (")).append(bucket).append(":").append(key).append(")").toString());
            else
            if(getResponse.connection.getResponseCode() != 200)
            {
                Logger.getLogger(com.bombdiggity.util.HTTPUtils.class).error((new StringBuilder("S3Utils.S3RequestToFile: Invalide response code (")).append(bucket).append(":").append(key).append("): ").append(getResponse.connection.getResponseCode()).toString());
            } else
            {
                DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(file, false));
                dataOut.write(getResponse.object.data);
                dataOut.flush();
                dataOut.close();
            }
            success = true;
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.HTTPUtils.class).error((new StringBuilder("S3Utils.S3RequestToFile: (")).append(bucket).append(":").append(key).append(")").append(e.toString()).toString());
            e.printStackTrace();
        }
        return success;
    }
}