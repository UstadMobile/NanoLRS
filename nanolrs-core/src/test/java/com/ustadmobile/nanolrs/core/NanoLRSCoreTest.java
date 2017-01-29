package com.ustadmobile.nanolrs.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mike on 1/21/17.
 */

public abstract class NanoLRSCoreTest {

    /**
     * Reads a given input stream (e.g. test resource json file etc) as a String.  We could use
     * Apache IO Commons here but that makes use of j2objc that little bit more complex hence
     * including it here for now using vanilla java
     *
     * @param in
     *
     * @return
     * @throws IOException
     */
    public String readInputStream(InputStream in) throws IOException {
        byte[] buf = new byte[1024];
        int bytesRead;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        while((bytesRead = in.read(buf)) != -1) {
            bout.write(buf, 0, bytesRead);
        }
        in.close();
        return new String(bout.toByteArray(), "UTF-8");
    }

    public abstract Object getContext();

}
