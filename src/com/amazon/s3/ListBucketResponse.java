
package com.amazon.s3;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

// Referenced classes of package com.amazon.s3:
//            Response, Utils, ListEntry, Owner, 
//            CommonPrefixEntry

public class ListBucketResponse extends Response
{
    class ListBucketHandler extends DefaultHandler
    {

        public void startDocument()
        {
            isEchoedPrefix = true;
        }

        public void endDocument()
        {
        }

        public void startElement(String uri, String name, String qName, Attributes attrs)
        {
            if(name.equals("Contents"))
                keyEntry = new ListEntry();
            else
            if(name.equals("Owner"))
                keyEntry.owner = new Owner();
            else
            if(name.equals("CommonPrefixes"))
                commonPrefixEntry = new CommonPrefixEntry();
        }

        public void endElement(String uri, String name, String qName)
        {
            if(name.equals("Name"))
                this.name = currText.toString();
            else
            if(name.equals("Prefix") && isEchoedPrefix)
            {
                prefix = currText.toString();
                isEchoedPrefix = false;
            } else
            if(name.equals("Marker"))
                marker = currText.toString();
            else
            if(name.equals("MaxKeys"))
                maxKeys = Integer.parseInt(currText.toString());
            else
            if(name.equals("Delimiter"))
                delimiter = currText.toString();
            else
            if(name.equals("IsTruncated"))
                isTruncated = Boolean.valueOf(currText.toString()).booleanValue();
            else
            if(name.equals("NextMarker"))
                nextMarker = currText.toString();
            else
            if(name.equals("Contents"))
                keyEntries.add(keyEntry);
            else
            if(name.equals("Key"))
                keyEntry.key = currText.toString();
            else
            if(name.equals("LastModified"))
                try
                {
                    keyEntry.lastModified = iso8601Parser.parse(currText.toString());
                }
                catch(ParseException e)
                {
                    throw new RuntimeException("Unexpected date format in list bucket output", e);
                }
            else
            if(name.equals("ETag"))
                keyEntry.eTag = currText.toString();
            else
            if(name.equals("Size"))
                keyEntry.size = Long.parseLong(currText.toString());
            else
            if(name.equals("StorageClass"))
                keyEntry.storageClass = currText.toString();
            else
            if(name.equals("ID"))
                keyEntry.owner.id = currText.toString();
            else
            if(name.equals("DisplayName"))
                keyEntry.owner.displayName = currText.toString();
            else
            if(name.equals("CommonPrefixes"))
                commonPrefixEntries.add(commonPrefixEntry);
            else
            if(name.equals("Prefix"))
                commonPrefixEntry.prefix = currText.toString();
            if(currText.length() != 0)
                currText = new StringBuffer();
        }

        public void characters(char ch[], int start, int length)
        {
            currText.append(ch, start, length);
        }

        public String getName()
        {
            return name;
        }

        public String getPrefix()
        {
            return prefix;
        }

        public String getMarker()
        {
            return marker;
        }

        public String getDelimiter()
        {
            return delimiter;
        }

        public int getMaxKeys()
        {
            return maxKeys;
        }

        public boolean getIsTruncated()
        {
            return isTruncated;
        }

        public String getNextMarker()
        {
            return nextMarker;
        }

        public List getKeyEntries()
        {
            return keyEntries;
        }

        public List getCommonPrefixEntries()
        {
            return commonPrefixEntries;
        }

        private String name;
        private String prefix;
        private String marker;
        private String delimiter;
        private int maxKeys;
        private boolean isTruncated;
        private String nextMarker;
        private boolean isEchoedPrefix;
        private List keyEntries;
        private ListEntry keyEntry;
        private List commonPrefixEntries;
        private CommonPrefixEntry commonPrefixEntry;
        private StringBuffer currText;
        private SimpleDateFormat iso8601Parser;
        final ListBucketResponse this$0;

        public ListBucketHandler()
        {
            this$0 = ListBucketResponse.this;
            //super();
            name = null;
            prefix = null;
            marker = null;
            delimiter = null;
            maxKeys = 0;
            isTruncated = false;
            nextMarker = null;
            isEchoedPrefix = false;
            keyEntries = null;
            keyEntry = null;
            commonPrefixEntries = null;
            commonPrefixEntry = null;
            currText = null;
            iso8601Parser = null;
            keyEntries = new ArrayList();
            commonPrefixEntries = new ArrayList();
            iso8601Parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            iso8601Parser.setTimeZone(new SimpleTimeZone(0, "GMT"));
            currText = new StringBuffer();
        }
    }


    public ListBucketResponse(HttpURLConnection connection)
        throws IOException
    {
        super(connection);
        name = null;
        prefix = null;
        marker = null;
        delimiter = null;
        maxKeys = 0;
        isTruncated = false;
        nextMarker = null;
        entries = null;
        commonPrefixEntries = null;
        if(connection.getResponseCode() < 400)
            try
            {
                XMLReader xr = Utils.createXMLReader();
                ListBucketHandler handler = new ListBucketHandler();
                xr.setContentHandler(handler);
                xr.setErrorHandler(handler);
                xr.parse(new InputSource(connection.getInputStream()));
                name = handler.getName();
                prefix = handler.getPrefix();
                marker = handler.getMarker();
                delimiter = handler.getDelimiter();
                maxKeys = handler.getMaxKeys();
                isTruncated = handler.getIsTruncated();
                nextMarker = handler.getNextMarker();
                entries = handler.getKeyEntries();
                commonPrefixEntries = handler.getCommonPrefixEntries();
            }
            catch(SAXException e)
            {
                throw new RuntimeException("Unexpected error parsing ListBucket xml", e);
            }
    }

    public String name;
    public String prefix;
    public String marker;
    public String delimiter;
    public int maxKeys;
    public boolean isTruncated;
    public String nextMarker;
    public List entries;
    public List commonPrefixEntries;
}