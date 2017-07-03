package com.ustadmobile.nanolrs.core.model;

import com.ustadmobile.nanolrs.core.PrimaryKeyAnnotationClass;

/**
 * Created by mike on 9/27/16.
 */

public interface User extends NanoLrsModelSyncable {

    /**
     * @nanolrs.primarykey
     *
     * @return
     */
    @PrimaryKeyAnnotationClass(str="pk")
    String getUuid();

    void setUuid(String id);

    public String getUsername();

    public void setUsername(String username);

    public String getPassword();

    public void setPassword(String password);

}
