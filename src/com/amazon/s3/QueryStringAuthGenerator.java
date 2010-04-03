
package com.amazon.s3;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

// Referenced classes of package com.amazon.s3:
//            CallingFormat, Utils, S3Object

public class QueryStringAuthGenerator
{

    public QueryStringAuthGenerator(String awsAccessKeyId, String awsSecretAccessKey)
    {
        this(awsAccessKeyId, awsSecretAccessKey, true);
    }

    public QueryStringAuthGenerator(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure)
    {
        this(awsAccessKeyId, awsSecretAccessKey, isSecure, "s3.amazonaws.com");
    }

    public QueryStringAuthGenerator(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server)
    {
        this(awsAccessKeyId, awsSecretAccessKey, isSecure, server, isSecure ? 443 : 80);
    }

    public QueryStringAuthGenerator(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server, int port)
    {
        this(awsAccessKeyId, awsSecretAccessKey, isSecure, server, port, CallingFormat.getSubdomainCallingFormat());
    }

    public QueryStringAuthGenerator(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server, CallingFormat callingFormat)
    {
        this(awsAccessKeyId, awsSecretAccessKey, isSecure, server, isSecure ? 443 : 80, callingFormat);
    }

    public QueryStringAuthGenerator(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server, int port, CallingFormat callingFormat)
    {
        expiresIn = null;
        expires = null;
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.isSecure = isSecure;
        this.server = server;
        this.port = port;
        this.callingFormat = callingFormat;
        expiresIn = DEFAULT_EXPIRES_IN;
        expires = null;
    }

    public void setCallingFormat(CallingFormat format)
    {
        callingFormat = format;
    }

    public void setExpires(long millisSinceEpoch)
    {
        expires = new Long(millisSinceEpoch);
        expiresIn = null;
    }

    public void setExpiresIn(long millis)
    {
        expiresIn = new Long(millis);
        expires = null;
    }

    public String createBucket(String bucket, Map headers)
    {
        if(!Utils.validateBucketName(bucket, callingFormat, false))
        {
            throw new IllegalArgumentException((new StringBuilder("Invalid Bucket Name: ")).append(bucket).toString());
        } else
        {
            Map pathArgs = new HashMap();
            return generateURL("PUT", bucket, "", pathArgs, headers);
        }
    }

    public String listBucket(String bucket, String prefix, String marker, Integer maxKeys, Map headers)
    {
        return listBucket(bucket, prefix, marker, maxKeys, null, headers);
    }

    public String listBucket(String bucket, String prefix, String marker, Integer maxKeys, String delimiter, Map headers)
    {
        Map pathArgs = Utils.paramsForListOptions(prefix, marker, maxKeys, delimiter);
        return generateURL("GET", bucket, "", pathArgs, headers);
    }

    public String deleteBucket(String bucket, Map headers)
    {
        Map pathArgs = new HashMap();
        return generateURL("DELETE", bucket, "", pathArgs, headers);
    }

    public String put(String bucket, String key, S3Object object, Map headers)
    {
        Map metadata = null;
        Map pathArgs = new HashMap();
        if(object != null)
            metadata = object.metadata;
        return generateURL("PUT", bucket, Utils.urlencode(key), pathArgs, mergeMeta(headers, metadata));
    }

    public String get(String bucket, String key, Map headers)
    {
        Map pathArgs = new HashMap();
        return generateURL("GET", bucket, Utils.urlencode(key), pathArgs, headers);
    }

    public String delete(String bucket, String key, Map headers)
    {
        Map pathArgs = new HashMap();
        return generateURL("DELETE", bucket, Utils.urlencode(key), pathArgs, headers);
    }

    public String getBucketLogging(String bucket, Map headers)
    {
        Map pathArgs = new HashMap();
        pathArgs.put("logging", null);
        return generateURL("GET", bucket, "", pathArgs, headers);
    }

    public String putBucketLogging(String bucket, String loggingXMLDoc, Map headers)
    {
        Map pathArgs = new HashMap();
        pathArgs.put("logging", null);
        return generateURL("PUT", bucket, "", pathArgs, headers);
    }

    public String getBucketACL(String bucket, Map headers)
    {
        return getACL(bucket, "", headers);
    }

    public String getACL(String bucket, String key, Map headers)
    {
        Map pathArgs = new HashMap();
        pathArgs.put("acl", null);
        return generateURL("GET", bucket, Utils.urlencode(key), pathArgs, headers);
    }

    public String putBucketACL(String bucket, String aclXMLDoc, Map headers)
    {
        return putACL(bucket, "", aclXMLDoc, headers);
    }

    public String putACL(String bucket, String key, String aclXMLDoc, Map headers)
    {
        Map pathArgs = new HashMap();
        pathArgs.put("acl", null);
        return generateURL("PUT", bucket, Utils.urlencode(key), pathArgs, headers);
    }

    public String listAllMyBuckets(Map headers)
    {
        Map pathArgs = new HashMap();
        return generateURL("GET", "", "", pathArgs, headers);
    }

    public String makeBareURL(String bucket, String key)
    {
        StringBuffer buffer = new StringBuffer();
        if(isSecure)
            buffer.append("https://");
        else
            buffer.append("http://");
        buffer.append(server).append(":").append(port).append("/").append(bucket);
        buffer.append("/").append(Utils.urlencode(key));
        return buffer.toString();
    }

    private String generateURL(String method, String bucketName, String key, Map pathArgs, Map headers)
    {
        long expires = 0L;
        if(expiresIn != null)
            expires = System.currentTimeMillis() + expiresIn.longValue();
        else
        if(this.expires != null)
            expires = this.expires.longValue();
        else
            throw new RuntimeException("Illegal expires state");
        expires /= 1000L;
        String canonicalString = Utils.makeCanonicalString(method, bucketName, key, pathArgs, headers, (new StringBuilder()).append(expires).toString());
        String encodedCanonical = Utils.encode(awsSecretAccessKey, canonicalString, true);
        pathArgs.put("Signature", encodedCanonical);
        pathArgs.put("Expires", Long.toString(expires));
        pathArgs.put("AWSAccessKeyId", awsAccessKeyId);
        String returnString;
        try
        {
            URL url = callingFormat.getURL(isSecure, server, port, bucketName, key, pathArgs);
            returnString = url.toString();
        }
        catch(MalformedURLException e)
        {
            returnString = (new StringBuilder("Exception generating url ")).append(e).toString();
        }
        return returnString;
    }

    private Map mergeMeta(Map headers, Map metadata)
    {
        Map merged = new TreeMap();
        if(headers != null)
        {
            String key;
            for(Iterator i = headers.keySet().iterator(); i.hasNext(); merged.put(key, headers.get(key)))
                key = (String)i.next();

        }
        if(metadata != null)
        {
            for(Iterator i = metadata.keySet().iterator(); i.hasNext();)
            {
                String key = (String)i.next();
                String metadataKey = (new StringBuilder("x-amz-meta-")).append(key).toString();
                if(merged.containsKey(metadataKey))
                    ((List)merged.get(metadataKey)).addAll((List)metadata.get(key));
                else
                    merged.put(metadataKey, metadata.get(key));
            }

        }
        return merged;
    }

    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private boolean isSecure;
    private String server;
    private int port;
    private CallingFormat callingFormat;
    private Long expiresIn;
    private Long expires;
    private static final Long DEFAULT_EXPIRES_IN = new Long(60000L);

}