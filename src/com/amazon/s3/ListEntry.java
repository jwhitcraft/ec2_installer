
package com.amazon.s3;

import java.util.Date;

// Referenced classes of package com.amazon.s3:
//            Owner

public class ListEntry
{

    public ListEntry()
    {
    }

    public String toString()
    {
        return key;
    }

    public String key;
    public Date lastModified;
    public String eTag;
    public long size;
    public String storageClass;
    public Owner owner;
}