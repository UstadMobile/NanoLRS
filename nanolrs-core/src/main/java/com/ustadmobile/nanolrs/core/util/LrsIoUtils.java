package com.ustadmobile.nanolrs.core.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mikedawson on 29/01/2017.
 */

public class LrsIoUtils {

    /**
     * Read the given inputstream fully to a String, then close the input stream
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static final String inputStreamToString(InputStream in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        copy(in, bout);
        in.close();
        return new String(bout.toByteArray(), "UTF-8");
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int bytesRead;

        while((bytesRead = in.read(buf)) != -1) {
            out.write(buf, 0, bytesRead);
        }
    }

    public static void closeQuietly(Closeable closeable)  {
        if(closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {}
        }
    }

}
