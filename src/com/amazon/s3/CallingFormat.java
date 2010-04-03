
package com.amazon.s3;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

// Referenced classes of package com.amazon.s3:
//            Utils

public abstract class CallingFormat
{
    private static class PathCallingFormat extends CallingFormat
    {

        public boolean supportsLocatedBuckets()
        {
            return false;
        }

        public String getPathBase(String bucket, String key)
        {
            return isBucketSpecified(bucket) ? (new StringBuilder("/")).append(bucket).append("/").append(key).toString() : "/";
        }

        public String getEndpoint(String server, int port, String bucket)
        {
            return (new StringBuilder(String.valueOf(server))).append(":").append(port).toString();
        }

        public URL getURL(boolean isSecure, String server, int port, String bucket, String key, Map pathArgs)
            throws MalformedURLException
        {
            String pathBase = isBucketSpecified(bucket) ? (new StringBuilder("/")).append(bucket).append("/").append(key).toString() : "/";
            String pathArguments = Utils.convertPathArgsHashToString(pathArgs);
            return new URL(isSecure ? "https" : "http", server, port, (new StringBuilder(String.valueOf(pathBase))).append(pathArguments).toString());
        }

        private boolean isBucketSpecified(String bucket)
        {
            if(bucket == null)
                return false;
            return bucket.length() != 0;
        }

        private PathCallingFormat()
        {
        }

        PathCallingFormat(PathCallingFormat pathcallingformat)
        {
            this();
        }
    }

    private static class SubdomainCallingFormat extends CallingFormat
    {

        public boolean supportsLocatedBuckets()
        {
            return true;
        }

        public String getServer(String server, String bucket)
        {
            return (new StringBuilder(String.valueOf(bucket))).append(".").append(server).toString();
        }

        public String getEndpoint(String server, int port, String bucket)
        {
            return (new StringBuilder(String.valueOf(getServer(server, bucket)))).append(":").append(port).toString();
        }

        public String getPathBase(String bucket, String key)
        {
            return (new StringBuilder("/")).append(key).toString();
        }

        public URL getURL(boolean isSecure, String server, int port, String bucket, String key, Map pathArgs)
            throws MalformedURLException
        {
            if(bucket == null || bucket.length() == 0)
            {
                String pathArguments = Utils.convertPathArgsHashToString(pathArgs);
                return new URL(isSecure ? "https" : "http", server, port, (new StringBuilder("/")).append(pathArguments).toString());
            } else
            {
                String serverToUse = getServer(server, bucket);
                String pathBase = getPathBase(bucket, key);
                String pathArguments = Utils.convertPathArgsHashToString(pathArgs);
                return new URL(isSecure ? "https" : "http", serverToUse, port, (new StringBuilder(String.valueOf(pathBase))).append(pathArguments).toString());
            }
        }

        private SubdomainCallingFormat()
        {
        }

        SubdomainCallingFormat(SubdomainCallingFormat subdomaincallingformat)
        {
            this();
        }

        SubdomainCallingFormat(SubdomainCallingFormat subdomaincallingformat, SubdomainCallingFormat subdomaincallingformat1)
        {
            this();
        }
    }

    private static class VanityCallingFormat extends SubdomainCallingFormat
    {

        public String getServer(String server, String bucket)
        {
            return bucket;
        }

        private VanityCallingFormat()
        {
            super(null, null);
        }

        VanityCallingFormat(VanityCallingFormat vanitycallingformat)
        {
            this();
        }
    }


    public CallingFormat()
    {
    }

    public abstract boolean supportsLocatedBuckets();

    public abstract String getEndpoint(String s, int i, String s1);

    public abstract String getPathBase(String s, String s1);

    public abstract URL getURL(boolean flag, String s, int i, String s1, String s2, Map map)
        throws MalformedURLException;

    public static CallingFormat getPathCallingFormat()
    {
        return pathCallingFormat;
    }

    public static CallingFormat getSubdomainCallingFormat()
    {
        return subdomainCallingFormat;
    }

    public static CallingFormat getVanityCallingFormat()
    {
        return vanityCallingFormat;
    }

    protected static CallingFormat pathCallingFormat = new PathCallingFormat(null);
    protected static CallingFormat subdomainCallingFormat = new SubdomainCallingFormat(null);
    protected static CallingFormat vanityCallingFormat = new VanityCallingFormat(null);

}