
package com.amazon.s3;

import com.amazon.thirdparty.Base64;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

// Referenced classes of package com.amazon.s3:
//            CallingFormat

public class Utils
{

    public Utils()
    {
    }

    static String makeCanonicalString(String method, String bucket, String key, Map pathArgs, Map headers)
    {
        return makeCanonicalString(method, bucket, key, pathArgs, headers, null);
    }

    static String makeCanonicalString(String method, String bucketName, String key, Map pathArgs, Map headers, String expires)
    {
        StringBuffer buf = new StringBuffer();
        buf.append((new StringBuilder(String.valueOf(method))).append("\n").toString());
        SortedMap interestingHeaders = new TreeMap();
        if(headers != null)
        {
            for(Iterator i = headers.keySet().iterator(); i.hasNext();)
            {
                String hashKey = (String)i.next();
                if(hashKey != null)
                {
                    String lk = hashKey.toLowerCase();
                    if(lk.equals("content-type") || lk.equals("content-md5") || lk.equals("date") || lk.startsWith("x-amz-"))
                    {
                        List s = (List)headers.get(hashKey);
                        interestingHeaders.put(lk, concatenateList(s));
                    }
                }
            }

        }
        if(interestingHeaders.containsKey("x-amz-date"))
            interestingHeaders.put("date", "");
        if(expires != null)
            interestingHeaders.put("date", expires);
        if(!interestingHeaders.containsKey("content-type"))
            interestingHeaders.put("content-type", "");
        if(!interestingHeaders.containsKey("content-md5"))
            interestingHeaders.put("content-md5", "");
        for(Iterator i = interestingHeaders.keySet().iterator(); i.hasNext(); buf.append("\n"))
        {
            String headerKey = (String)i.next();
            if(headerKey.startsWith("x-amz-"))
                buf.append(headerKey).append(':').append(interestingHeaders.get(headerKey));
            else
                buf.append(interestingHeaders.get(headerKey));
        }

        if(bucketName != null && !bucketName.equals(""))
            buf.append((new StringBuilder("/")).append(bucketName).toString());
        buf.append("/");
        if(key != null)
            buf.append(key);
        if(pathArgs != null)
            if(pathArgs.containsKey("acl"))
                buf.append("?acl");
            else
            if(pathArgs.containsKey("torrent"))
                buf.append("?torrent");
            else
            if(pathArgs.containsKey("logging"))
                buf.append("?logging");
            else
            if(pathArgs.containsKey("location"))
                buf.append("?location");
        return buf.toString();
    }

    static String encode(String awsSecretAccessKey, String canonicalString, boolean urlencode)
    {
        SecretKeySpec signingKey = new SecretKeySpec(awsSecretAccessKey.getBytes(), "HmacSHA1");
        Mac mac = null;
        try
        {
            mac = Mac.getInstance("HmacSHA1");
        }
        catch(NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Could not find sha1 algorithm", e);
        }
        try
        {
            mac.init(signingKey);
        }
        catch(InvalidKeyException e)
        {
            throw new RuntimeException("Could not initialize the MAC algorithm", e);
        }
        String b64 = Base64.encodeBytes(mac.doFinal(canonicalString.getBytes()));
        if(urlencode)
            return urlencode(b64);
        else
            return b64;
    }

    static Map paramsForListOptions(String prefix, String marker, Integer maxKeys)
    {
        return paramsForListOptions(prefix, marker, maxKeys, null);
    }

    static Map paramsForListOptions(String prefix, String marker, Integer maxKeys, String delimiter)
    {
        Map argParams = new HashMap();
        if(prefix != null)
            argParams.put("prefix", urlencode(prefix));
        if(marker != null)
            argParams.put("marker", urlencode(marker));
        if(delimiter != null)
            argParams.put("delimiter", urlencode(delimiter));
        if(maxKeys != null)
            argParams.put("max-keys", Integer.toString(maxKeys.intValue()));
        return argParams;
    }

    public static String convertPathArgsHashToString(Map pathArgs)
    {
        StringBuffer pathArgsString = new StringBuffer();
        boolean firstRun = true;
        if(pathArgs != null)
        {
            for(Iterator argumentIterator = pathArgs.keySet().iterator(); argumentIterator.hasNext();)
            {
                String argument = (String)argumentIterator.next();
                if(firstRun)
                {
                    firstRun = false;
                    pathArgsString.append("?");
                } else
                {
                    pathArgsString.append("&");
                }
                String argumentValue = (String)pathArgs.get(argument);
                pathArgsString.append(argument);
                if(argumentValue != null)
                {
                    pathArgsString.append("=");
                    pathArgsString.append(argumentValue);
                }
            }

        }
        return pathArgsString.toString();
    }

    static String urlencode(String unencoded)
    {
        try
        {
            return URLEncoder.encode(unencoded, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            throw new RuntimeException("Could not url encode to UTF-8", e);
        }
    }

    static XMLReader createXMLReader()
    {
        try
        {
            return XMLReaderFactory.createXMLReader();
        }
        catch(SAXException e)
        {
            System.setProperty("org.xml.sax.driver", "org.apache.crimson.parser.XMLReaderImpl");
        }
        try
        {
            return XMLReaderFactory.createXMLReader();
        }
        catch(SAXException e)
        {
            throw new RuntimeException("Couldn't initialize a sax driver for the XMLReader");
        }
    }

    private static String concatenateList(List values)
    {
        StringBuffer buf = new StringBuffer();
        int i = 0;
        for(int size = values.size(); i < size; i++)
        {
            buf.append(((String)values.get(i)).replaceAll("\n", "").trim());
            if(i != size - 1)
                buf.append(",");
        }

        return buf.toString();
    }

    static boolean validateBucketName(String bucketName, CallingFormat callingFormat, boolean located)
    {
        int MIN_BUCKET_LENGTH;
        int MAX_BUCKET_LENGTH;
        if(callingFormat == CallingFormat.getPathCallingFormat())
        {
            MIN_BUCKET_LENGTH = 3;
            MAX_BUCKET_LENGTH = 255;
            String BUCKET_NAME_REGEX = "^[0-9A-Za-z\\.\\-_]*$";
            return bucketName != null && bucketName.length() >= 3 && bucketName.length() <= 255 && bucketName.matches("^[0-9A-Za-z\\.\\-_]*$");
        }
        MIN_BUCKET_LENGTH = 3;
        MAX_BUCKET_LENGTH = 63;
        String IPv4_REGEX = "^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$";
        String BUCKET_NAME_REGEX = "^[a-z0-9]([a-z0-9\\-]*[a-z0-9])?(\\.[a-z0-9]([a-z0-9\\-]*[a-z0-9])?)*$";
        return bucketName != null && bucketName.length() >= 3 && bucketName.length() <= 63 && !bucketName.matches("^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$") && bucketName.matches("^[a-z0-9]([a-z0-9\\-]*[a-z0-9])?(\\.[a-z0-9]([a-z0-9\\-]*[a-z0-9])?)*$");
    }

    static final String METADATA_PREFIX = "x-amz-meta-";
    static final String AMAZON_HEADER_PREFIX = "x-amz-";
    static final String ALTERNATIVE_DATE_HEADER = "x-amz-date";
    public static final String DEFAULT_HOST = "s3.amazonaws.com";
    public static final int SECURE_PORT = 443;
    public static final int INSECURE_PORT = 80;
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
}