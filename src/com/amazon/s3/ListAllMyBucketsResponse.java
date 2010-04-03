
package com.amazon.s3;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

// Referenced classes of package com.amazon.s3:
//            Response, Utils, Bucket

public class ListAllMyBucketsResponse extends Response
{
    static class ListAllMyBucketsHandler extends DefaultHandler
    {

        public void startDocument()
        {
        }

        public void endDocument()
        {
        }

        public void startElement(String uri, String name, String qName, Attributes attrs)
        {
            if(name.equals("Bucket"))
                currBucket = new Bucket();
        }

        public void endElement(String uri, String name, String qName)
        {
            if(name.equals("Bucket"))
                entries.add(currBucket);
            else
            if(name.equals("Name"))
                currBucket.name = currText.toString();
            else
            if(name.equals("CreationDate"))
                try
                {
                    currBucket.creationDate = iso8601Parser.parse(currText.toString());
                }
                catch(ParseException e)
                {
                    throw new RuntimeException("Unexpected date format in list bucket output", e);
                }
            currText = new StringBuffer();
        }

        public void characters(char ch[], int start, int length)
        {
            currText.append(ch, start, length);
        }

        public List getEntries()
        {
            return entries;
        }

        private List entries;
        private Bucket currBucket;
        private StringBuffer currText;
        private SimpleDateFormat iso8601Parser;

        public ListAllMyBucketsHandler()
        {
            entries = null;
            currBucket = null;
            currText = null;
            iso8601Parser = null;
            entries = new ArrayList();
            iso8601Parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            iso8601Parser.setTimeZone(new SimpleTimeZone(0, "GMT"));
            currText = new StringBuffer();
        }
    }


    public ListAllMyBucketsResponse(HttpURLConnection connection)
        throws IOException
    {
        super(connection);
        if(connection.getResponseCode() < 400)
            try
            {
                XMLReader xr = Utils.createXMLReader();
                ListAllMyBucketsHandler handler = new ListAllMyBucketsHandler();
                xr.setContentHandler(handler);
                xr.setErrorHandler(handler);
                xr.parse(new InputSource(connection.getInputStream()));
                entries = handler.getEntries();
            }
            catch(SAXException e)
            {
                throw new RuntimeException("Unexpected error parsing ListAllMyBuckets xml", e);
            }
    }

    public List entries;
}