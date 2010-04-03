
package com.bombdiggity.amazon.ec2.install;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class InstallSession
{

    public InstallSession(File baseFolder)
    {
        this.baseFolder = null;
        environment = new HashMap<String, String>();
        this.baseFolder = baseFolder;
    }

    public File getBaseFolder()
    {
        return baseFolder;
    }

    public void setBaseFolder(File baseFolder)
    {
        this.baseFolder = baseFolder;
    }

    public void setEnvironmentValue(String key, String value)
    {
        environment.put(key, value);
    }

    public String getEnvironmentValue(String key)
    {
        return (String)environment.get(key);
    }

    public Map<String, String> getEnvironmentMap()
    {
        return environment;
    }

    private File baseFolder;
    private Map<String, String> environment;
}