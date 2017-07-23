package com.ustadmobile.nanolrs.core.model;
/**
 * Created by varuna on 7/22/2017.
 */

import com.ustadmobile.nanolrs.core.model.NanoLrsModel;

public interface Group extends NanoLrsModel {
    /**
     * Tells the generator that this is the primary key.
     *
     * @return
     * @nanolrs.primarykey
     */
    String getUUID();

    void setUUID(String uuid);

    public String getName();

    public void setName(String name);

    public String getDesc();

    public void setDesc(String desc);

    public String getLocation();
    public void setLocation(String location);

    //Role in group ?

    //What else in group?

}
