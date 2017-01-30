package com.ustadmobile.nanolrs.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
        byte[] buf = new byte[1024];
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int bytesRead;

        while((bytesRead = in.read(buf)) != -1) {
            bout.write(buf, 0, bytesRead);
        }

        return new String(bout.toByteArray(), "UTF-8");
    }

}
