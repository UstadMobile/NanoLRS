package com.ustadmobile.nanolrs.core.model;
/**
 * Created by varuna on 7/22/2017.
 */

import com.ustadmobile.nanolrs.core.model.NanoLrsModel;

public interface Role extends NanoLrsModel {
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

    String getDesc();
    void setDesc(String desc);
}
