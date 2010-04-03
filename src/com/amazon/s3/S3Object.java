
package com.amazon.s3;

import java.util.Map;

public class S3Object
{

    public S3Object(byte data[], Map metadata)
    {
        this.data = data;
        this.metadata = metadata;
    }

    public byte data[];
    public Map metadata;
}