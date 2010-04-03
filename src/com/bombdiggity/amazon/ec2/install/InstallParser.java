
package com.bombdiggity.amazon.ec2.install;

import com.bombdiggity.util.XMLUtils;

import java.io.File;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import org.apache.log4j.Logger;
import org.w3c.dom.*;

// Referenced classes of package com.bombdiggity.amazon.ec2.install:
//            Installer, InstallCommands, InstallSession

public class InstallParser
{

    public InstallParser(InstallSession session)
    {
        this.session = null;
        this.session = session;
    }

    private Document loadFile(File file)
    {
        Document doc = null;
        try
        {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
            doc = domBuilder.parse(file);
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallParser.class).error((new StringBuilder("InstallParser.loadFile: ")).append(e.toString()).toString());
            e.printStackTrace();
        }
        return doc;
    }

    public void parseStartup(File file)
    {
        Logger.getLogger(com.bombdiggity.amazon.ec2.install.Installer.class).info((new StringBuilder("InstallParser.parseStartup: ")).append(file).toString());
        try
        {
            Document doc = loadFile(file);
            if(doc != null)
            {
                XPathFactory factory = XMLUtils.newXPathFactory();
                XPath xpath = factory.newXPath();
                String rootXPath = "/Startup/Commands/*";
                Element root = doc.getDocumentElement();
                XPathExpression rootExp = xpath.compile(rootXPath);
                NodeList streamList = (NodeList)rootExp.evaluate(root, XPathConstants.NODESET);
                if(streamList != null)
                {
                    for(int i = 0; i < streamList.getLength(); i++)
                    {
                        Node streamNode = streamList.item(i);
                        Element streamElem = (Element)streamNode;
                        if(streamElem.getNodeName().toLowerCase().equals("install"))
                        {
                            String packageName = null;
                            String folderPath = null;
                            for(Node child = streamNode.getFirstChild(); child != null; child = child.getNextSibling())
                                if(child.getNodeName().toLowerCase().equals("package"))
                                    packageName = XMLUtils.getNodeValue(child).trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("folder"))
                                    folderPath = XMLUtils.getNodeValue(child).trim();

                            if(packageName != null)
                                InstallCommands.installPackage(session, packageName);
                            else
                            if(folderPath != null)
                                InstallCommands.installFolder(session, folderPath);
                            else
                                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallParser.class).error("StartupParser.loadFile: <Install>: <Package> or <Folder> required");
                        } else
                        if(streamElem.getNodeName().toLowerCase().equals("download"))
                        {
                            String url = null;
                            String method = "get";
                            String data = null;
                            String destination = "/opt";
                            String action = null;
                            List headers = new ArrayList();
                            for(Node child = streamNode.getFirstChild(); child != null; child = child.getNextSibling())
                                if(child.getNodeName().toLowerCase().equals("url"))
                                    url = XMLUtils.getNodeValue(child).trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("method"))
                                    method = XMLUtils.getNodeValue(child).toLowerCase().trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("data"))
                                    data = XMLUtils.getNodeValue(child).trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("destination"))
                                    destination = XMLUtils.getNodeValue(child).trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("action"))
                                    action = XMLUtils.getNodeValue(child).toLowerCase().trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("header"))
                                {
                                    Node nameNode = XMLUtils.getNodeByTagName((Element)child, "Name");
                                    Node valueNode = XMLUtils.getNodeByTagName((Element)child, "Value");
                                    if(nameNode != null && valueNode != null)
                                    {
                                        Map namePair = new HashMap();
                                        namePair.put(XMLUtils.getNodeValue(nameNode).trim(), XMLUtils.getNodeValue(valueNode).trim());
                                        headers.add(namePair);
                                    } else
                                    {
                                        Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallParser.class).error("StartupParser.loadFile: <Download/Header>: <Name> and <Value> required");
                                    }
                                }

                            if(url != null && destination != null)
                                InstallCommands.downloadFile(session, url, method, data, headers, destination, action);
                            else
                                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallParser.class).error("StartupParser.loadFile: <Download>: <URL> and <Destination> required");
                        } else
                        if(streamElem.getNodeName().toLowerCase().equals("s3fetch"))
                        {
                            String awsAccessKeyId = null;
                            String awsSecretAccessKey = null;
                            String bucket = null;
                            String key = null;
                            String destination = null;
                            String action = null;
                            for(Node child = streamNode.getFirstChild(); child != null; child = child.getNextSibling())
                                if(child.getNodeName().toLowerCase().equals("awsaccesskeyid"))
                                    awsAccessKeyId = XMLUtils.getNodeValue(child).trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("awssecretaccesskey"))
                                    awsSecretAccessKey = XMLUtils.getNodeValue(child).trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("bucket"))
                                    bucket = XMLUtils.getNodeValue(child).trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("key"))
                                    key = XMLUtils.getNodeValue(child).trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("destination"))
                                    destination = XMLUtils.getNodeValue(child).trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("action"))
                                    action = XMLUtils.getNodeValue(child).toLowerCase().trim();

                            if(awsAccessKeyId != null && awsSecretAccessKey != null && bucket != null && key != null && destination != null)
                                InstallCommands.fetchFile(session, awsAccessKeyId, awsSecretAccessKey, bucket, key, destination, action);
                            else
                                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallParser.class).error("StartupParser.loadFile: <Fetch>: <AWSAccessKeyId>, <AWSSecretAccessKey>, <Bucket>, <Key> and <Destination> required");
                        } else
                        if(streamElem.getNodeName().toLowerCase().equals("runscript"))
                        {
                            String script = null;
                            List params = new ArrayList();
                            for(Node child = streamNode.getFirstChild(); child != null; child = child.getNextSibling())
                                if(child.getNodeName().toLowerCase().equals("script"))
                                    script = XMLUtils.getNodeValue(child).trim();
                                else
                                if(child.getNodeName().toLowerCase().equals("param"))
                                {
                                    String param = XMLUtils.getNodeValue(child).trim();
                                    params.add(param);
                                }

                            if(script != null)
                                InstallCommands.runScript(session, script, params);
                            else
                                Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallParser.class).error("StartupParser.loadFile: <RunScript>: <Script> required");
                        }
                    }

                }
            }
        }
        catch(Exception e)
        {
            Logger.getLogger(com.bombdiggity.amazon.ec2.install.InstallParser.class).error((new StringBuilder("InstallParser.parseStartup: ")).append(e.toString()).toString());
            e.printStackTrace();
        }
    }

    InstallSession session;
}