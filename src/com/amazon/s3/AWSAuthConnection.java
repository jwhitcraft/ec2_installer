
package com.amazon.s3;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

// Referenced classes of package com.amazon.s3:
//            CallingFormat, Utils, Response, ListBucketResponse, 
//            S3Object, GetResponse, LocationResponse, ListAllMyBucketsResponse

public class AWSAuthConnection
{

    public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey)
    {
        this(awsAccessKeyId, awsSecretAccessKey, true);
    }

    public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure)
    {
        this(awsAccessKeyId, awsSecretAccessKey, isSecure, "s3.amazonaws.com");
    }

    public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server)
    {
        this(awsAccessKeyId, awsSecretAccessKey, isSecure, server, isSecure ? 443 : 80);
    }

    public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server, int port)
    {
        this(awsAccessKeyId, awsSecretAccessKey, isSecure, server, port, CallingFormat.getSubdomainCallingFormat());
    }

    public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server, CallingFormat format)
    {
        this(awsAccessKeyId, awsSecretAccessKey, isSecure, server, isSecure ? 443 : 80, CallingFormat.getSubdomainCallingFormat());
    }

    public AWSAuthConnection(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server, int port, CallingFormat format)
    {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.isSecure = isSecure;
        this.server = server;
        this.port = port;
        callingFormat = format;
    }

    /**
     * @deprecated Method createBucket is deprecated
     */

    public Response createBucket(String bucket, Map headers)
        throws MalformedURLException, IOException
    {
        return createBucket(bucket, null, headers);
    }

    public Response createBucket(String bucket, String location, Map headers)
        throws MalformedURLException, IOException
    {
        String body;
        if(location == null)
            body = null;
        else
        if(LOCATION_EU.equals(location))
        {
            if(!callingFormat.supportsLocatedBuckets())
                throw new IllegalArgumentException("Creating location-constrained bucket with unsupported calling-format");
            body = (new StringBuilder("<CreateBucketConstraint><LocationConstraint>")).append(location).append("</LocationConstraint></CreateBucketConstraint>").toString();
        } else
        {
            throw new IllegalArgumentException((new StringBuilder("Invalid Location: ")).append(location).toString());
        }
        if(!Utils.validateBucketName(bucket, callingFormat, location != null))
            throw new IllegalArgumentException((new StringBuilder("Invalid Bucket Name: ")).append(bucket).toString());
        HttpURLConnection request = makeRequest("PUT", bucket, "", null, headers);
        if(body != null)
        {
            request.setDoOutput(true);
            request.getOutputStream().write(body.getBytes("UTF-8"));
        }
        return new Response(request);
    }

    public boolean checkBucketExists(String bucket)
        throws MalformedURLException, IOException
    {
        HttpURLConnection response = makeRequest("HEAD", bucket, "", null, null);
        int httpCode = response.getResponseCode();
        return httpCode >= 200 && httpCode < 300;
    }

    public ListBucketResponse listBucket(String bucket, String prefix, String marker, Integer maxKeys, Map headers)
        throws MalformedURLException, IOException
    {
        return listBucket(bucket, prefix, marker, maxKeys, null, headers);
    }

    public ListBucketResponse listBucket(String bucket, String prefix, String marker, Integer maxKeys, String delimiter, Map headers)
        throws MalformedURLException, IOException
    {
        Map pathArgs = Utils.paramsForListOptions(prefix, marker, maxKeys, delimiter);
        return new ListBucketResponse(makeRequest("GET", bucket, "", pathArgs, headers));
    }

    public Response deleteBucket(String bucket, Map headers)
        throws MalformedURLException, IOException
    {
        return new Response(makeRequest("DELETE", bucket, "", null, headers));
    }

    public Response put(String bucket, String key, S3Object object, Map headers)
        throws MalformedURLException, IOException
    {
        HttpURLConnection request = makeRequest("PUT", bucket, Utils.urlencode(key), null, headers, object);
        request.setDoOutput(true);
        request.getOutputStream().write(object.data != null ? object.data : new byte[0]);
        return new Response(request);
    }

    public GetResponse get(String bucket, String key, Map headers)
        throws MalformedURLException, IOException
    {
        return new GetResponse(makeRequest("GET", bucket, Utils.urlencode(key), null, headers));
    }

    public Response delete(String bucket, String key, Map headers)
        throws MalformedURLException, IOException
    {
        return new Response(makeRequest("DELETE", bucket, Utils.urlencode(key), null, headers));
    }

    public GetResponse getBucketLogging(String bucket, Map headers)
        throws MalformedURLException, IOException
    {
        Map pathArgs = new HashMap();
        pathArgs.put("logging", null);
        return new GetResponse(makeRequest("GET", bucket, "", pathArgs, headers));
    }

    public Response putBucketLogging(String bucket, String loggingXMLDoc, Map headers)
        throws MalformedURLException, IOException
    {
        Map pathArgs = new HashMap();
        pathArgs.put("logging", null);
        S3Object object = new S3Object(loggingXMLDoc.getBytes(), null);
        HttpURLConnection request = makeRequest("PUT", bucket, "", pathArgs, headers, object);
        request.setDoOutput(true);
        request.getOutputStream().write(object.data != null ? object.data : new byte[0]);
        return new Response(request);
    }

    public GetResponse getBucketACL(String bucket, Map headers)
        throws MalformedURLException, IOException
    {
        return getACL(bucket, "", headers);
    }

    public GetResponse getACL(String bucket, String key, Map headers)
        throws MalformedURLException, IOException
    {
        if(key == null)
            key = "";
        Map pathArgs = new HashMap();
        pathArgs.put("acl", null);
        return new GetResponse(makeRequest("GET", bucket, Utils.urlencode(key), pathArgs, headers));
    }

    public Response putBucketACL(String bucket, String aclXMLDoc, Map headers)
        throws MalformedURLException, IOException
    {
        return putACL(bucket, "", aclXMLDoc, headers);
    }

    public Response putACL(String bucket, String key, String aclXMLDoc, Map headers)
        throws MalformedURLException, IOException
    {
        S3Object object = new S3Object(aclXMLDoc.getBytes(), null);
        Map pathArgs = new HashMap();
        pathArgs.put("acl", null);
        HttpURLConnection request = makeRequest("PUT", bucket, Utils.urlencode(key), pathArgs, headers, object);
        request.setDoOutput(true);
        request.getOutputStream().write(object.data != null ? object.data : new byte[0]);
        return new Response(request);
    }

    public LocationResponse getBucketLocation(String bucket)
        throws MalformedURLException, IOException
    {
        Map pathArgs = new HashMap();
        pathArgs.put("location", null);
        return new LocationResponse(makeRequest("GET", bucket, "", pathArgs, null));
    }

    public ListAllMyBucketsResponse listAllMyBuckets(Map headers)
        throws MalformedURLException, IOException
    {
        return new ListAllMyBucketsResponse(makeRequest("GET", "", "", null, headers));
    }

    private HttpURLConnection makeRequest(String method, String bucketName, String key, Map pathArgs, Map headers)
        throws MalformedURLException, IOException
    {
        return makeRequest(method, bucketName, key, pathArgs, headers, null);
    }

    private HttpURLConnection makeRequest(String method, String bucket, String key, Map pathArgs, Map headers, S3Object object)
        throws MalformedURLException, IOException
    {
        URL url = callingFormat.getURL(isSecure, server, port, bucket, key, pathArgs);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod(method);
        if(!connection.getInstanceFollowRedirects() && callingFormat.supportsLocatedBuckets())
            throw new RuntimeException("HTTP redirect support required.");
        addHeaders(connection, headers);
        if(object != null)
            addMetadataHeaders(connection, object.metadata);
        addAuthHeader(connection, method, bucket, key, pathArgs);
        return connection;
    }

    private void addHeaders(HttpURLConnection connection, Map headers)
    {
        addHeaders(connection, headers, "");
    }

    private void addMetadataHeaders(HttpURLConnection connection, Map metadata)
    {
        addHeaders(connection, metadata, "x-amz-meta-");
    }

    private void addHeaders(HttpURLConnection connection, Map headers, String prefix)
    {
        if(headers != null)
        {
            for(Iterator i = headers.keySet().iterator(); i.hasNext();)
            {
                String key = (String)i.next();
                String value;
                for(Iterator j = ((List)headers.get(key)).iterator(); j.hasNext(); connection.addRequestProperty((new StringBuilder(String.valueOf(prefix))).append(key).toString(), value))
                    value = (String)j.next();

            }

        }
    }

    private void addAuthHeader(HttpURLConnection connection, String method, String bucket, String key, Map pathArgs)
    {
        if(connection.getRequestProperty("Date") == null)
            connection.setRequestProperty("Date", httpDate());
        if(connection.getRequestProperty("Content-Type") == null)
            connection.setRequestProperty("Content-Type", "");
        String canonicalString = Utils.makeCanonicalString(method, bucket, key, pathArgs, connection.getRequestProperties());
        String encodedCanonical = Utils.encode(awsSecretAccessKey, canonicalString, false);
        connection.setRequestProperty("Authorization", (new StringBuilder("AWS ")).append(awsAccessKeyId).append(":").append(encodedCanonical).toString());
    }

    public static String httpDate()
    {
        String DateFormat = "EEE, dd MMM yyyy HH:mm:ss ";
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return (new StringBuilder(String.valueOf(format.format(new Date())))).append("GMT").toString();
    }

    public static String LOCATION_DEFAULT = null;
    public static String LOCATION_EU = "EU";
    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private boolean isSecure;
    private String server;
    private int port;
    private CallingFormat callingFormat;

}