
package com.amazon.s3;

import java.io.IOException;
import java.net.HttpURLConnection;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

// Referenced classes of package com.amazon.s3:
//            Response, Utils

public class LocationResponse extends Response
{
    static class LocationResponseHandler extends DefaultHandler
    {

        public void startDocument()
        {
        }

        public void startElement(String uri, String name, String qName, Attributes attrs)
        {
            if(name.equals("LocationConstraint"))
                currText = new StringBuffer();
        }

        public void endElement(String uri, String name, String qName)
        {
            if(name.equals("LocationConstraint"))
            {
                location = currText.toString();
                currText = null;
            }
        }

        public void characters(char ch[], int start, int length)
        {
            if(currText != null)
                currText.append(ch, start, length);
        }

        String location;
        private StringBuffer currText;

        LocationResponseHandler()
        {
            location = null;
            currText = null;
        }
    }


    public LocationResponse(HttpURLConnection connection)
        throws IOException
    {
        super(connection);
        if(connection.getResponseCode() < 400)
            try
            {
                XMLReader xr = Utils.createXMLReader();
                LocationResponseHandler handler = new LocationResponseHandler();
                xr.setContentHandler(handler);
                xr.setErrorHandler(handler);
                xr.parse(new InputSource(connection.getInputStream()));
                location = handler.location;
            }
            catch(SAXException e)
            {
                throw new RuntimeException("Unexpected error parsing ListAllMyBuckets xml", e);
            }
        else
            location = "<error>";
    }

    public String getLocation()
    {
        return location;
    }

    String location;
}