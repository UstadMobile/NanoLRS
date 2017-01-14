package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.nanolrs.core.model.XapiVerb;

/**
 * Created by mike on 9/13/16.
 */
@DatabaseTable(tableName = "xapi_verb")
public class XapiVerbEntity implements XapiVerb {

    @DatabaseField(id = true)
    private String id;

    @DatabaseField
    private String canonicalData;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getCanonicalData() {
        return canonicalData;
    }

    @Override
    public void setCanonicalData(String canonicalData) {
        this.canonicalData = canonicalData;
    }
}
