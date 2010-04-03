
package com.amazon.s3;

import java.util.Date;

public class Bucket
{

    public Bucket()
    {
        name = null;
        creationDate = null;
    }

    public Bucket(String name, Date creationDate)
    {
        this.name = name;
        this.creationDate = creationDate;
    }

    public String toString()
    {
        return name;
    }

    public String name;
    public Date creationDate;
}