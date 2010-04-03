
package com.amazon.s3;

import java.io.IOException;
import java.net.HttpURLConnection;

public class Response
{

    public Response(HttpURLConnection connection)
        throws IOException
    {
        this.connection = connection;
    }

    public HttpURLConnection connection;
}