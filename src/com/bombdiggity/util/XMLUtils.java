
package com.bombdiggity.util;

import com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.*;

public class XMLUtils
{

    public XMLUtils()
    {
    }

    public static Node getNodeByTagName(Element node, String name)
    {
        NodeList nodeList = node.getElementsByTagName(name);
        if(nodeList != null && nodeList.getLength() > 0)
            return nodeList.item(0);
        else
            return null;
    }

    public static String getNodeValue(Node node)
    {
        String ret = "";
        if(node != null)
        {
            for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
                ret = (new StringBuilder(String.valueOf(ret))).append(child.getNodeValue()).toString();

        }
        return ret;
    }

    public static String getXMLPropertyStr(XPath xpath, String xpathStr, Element root)
    {
        return getXMLPropertyStr(xpath, xpathStr, root, null);
    }

    public static String getXMLPropertyStr(XPath xpath, String xpathStr, Element root, String defaultVal)
    {
        String ret = defaultVal;
        try
        {
            String value = xpath.evaluate(xpathStr, root);
            if(value.length() > 0)
                ret = value;
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.XMLUtils.class).debug((new StringBuilder("getXMLPropertyStr: ")).append(e.toString()).toString());
        }
        return ret;
    }

    public static int getXMLPropertyInt(XPath xpath, String xpathStr, Element root, int defaultVal)
    {
        int ret = defaultVal;
        try
        {
            String value = xpath.evaluate(xpathStr, root);
            if(value.length() > 0)
                ret = Integer.parseInt(value);
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.XMLUtils.class).error((new StringBuilder("getXMLPropertyInt: ")).append(e.toString()).toString());
        }
        return ret;
    }

    public static long getXMLPropertyLong(XPath xpath, String xpathStr, Element root, long defaultVal)
    {
        long ret = defaultVal;
        try
        {
            String value = xpath.evaluate(xpathStr, root);
            if(value.length() > 0)
                ret = Long.parseLong(value);
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.XMLUtils.class).error((new StringBuilder("getXMLPropertyLong: ")).append(e.toString()).toString());
        }
        return ret;
    }

    public static double getXMLPropertyDouble(XPath xpath, String xpathStr, Element root, double defaultVal)
    {
        double ret = defaultVal;
        try
        {
            String value = xpath.evaluate(xpathStr, root);
            if(value.length() > 0)
                ret = Double.parseDouble(value);
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.XMLUtils.class).error((new StringBuilder("getXMLPropertyDouble: ")).append(e.toString()).toString());
        }
        return ret;
    }

    public static boolean getXMLPropertyBool(XPath xpath, String xpathStr, Element root, boolean defaultVal)
    {
        boolean ret = defaultVal;
        try
        {
            String value = xpath.evaluate(xpathStr, root);
            if(value.length() > 0)
                ret = Boolean.parseBoolean(value);
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.util.XMLUtils.class).error((new StringBuilder("getXMLPropertyBool: ")).append(e.toString()).toString());
        }
        return ret;
    }

    public static XPathFactory newXPathFactory()
    {
        XPathFactory ret = null;
        if(ret == null)
            try
            {
                ret = XPathFactory.newInstance();
            }
            catch(Exception exception) { }
        if(ret == null)
            try
            {
                new XPathFactoryImpl();
            }
            catch(Exception exception1) { }
        if(ret == null)
            System.out.println("Error: Can't find XPathFactory");
        return ret;
    }
}