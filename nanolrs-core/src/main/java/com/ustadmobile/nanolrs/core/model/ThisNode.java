package com.ustadmobile.nanolrs.core.model;
/**
 * Created by varuna on 7/9/2017.
 */

import com.ustadmobile.nanolrs.core.model.NanoLrsModel;

public interface ThisNode extends NanoLrsModel {
    /**
     * Tells the generator that this is the primary key.
     *
     * @return
     * @nanolrs.primarykey
     */
    String getUUID();
    void setUUID(String uuid);

    String getName();
    void setName(String name);

    String getHost();
    void setHost(String host);

    String getUrl();
    void setUrl(String url);

    boolean isMaster();
    void setMaster(boolean master);

    boolean isProxy();
    void setProxy(boolean proxy);

    /* Any other roles */
    String getRole();
    void setRole(String role);
}
