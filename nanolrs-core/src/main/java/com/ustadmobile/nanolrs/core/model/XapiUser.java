package com.ustadmobile.nanolrs.core.model;

/**
 * Created by mike on 9/27/16.
 */

public interface XapiUser extends NanoLrsModel {



    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    String getUuid();

    void setUuid(String id);

    public String getUsername();

    public void setUsername(String username);

    public String getPassword();

    public void setPassword(String password);

}
