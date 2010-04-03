
package com.amazon.s3;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;

// Referenced classes of package com.amazon.s3:
//            Response, S3Object

public class GetResponse extends Response
{

    public GetResponse(HttpURLConnection connection)
        throws IOException
    {
        super(connection);
        if(connection.getResponseCode() < 400)
        {
            Map metadata = extractMetadata(connection);
            byte body[] = slurpInputStream(connection.getInputStream());
            object = new S3Object(body, metadata);
        }
    }

    private Map extractMetadata(HttpURLConnection connection)
    {
        TreeMap metadata = new TreeMap();
        Map headers = connection.getHeaderFields();
        for(Iterator i = headers.keySet().iterator(); i.hasNext();)
        {
            String key = (String)i.next();
            if(key != null && key.startsWith("x-amz-meta-"))
                metadata.put(key.substring("x-amz-meta-".length()), headers.get(key));
        }

        return metadata;
    }

    static byte[] slurpInputStream(InputStream stream)
        throws IOException
    {
        int chunkSize = 2048;
        byte buf[] = new byte[2048];
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(2048);
        int count;
        while((count = stream.read(buf)) != -1) 
            byteStream.write(buf, 0, count);
        return byteStream.toByteArray();
    }

    public S3Object object;
}