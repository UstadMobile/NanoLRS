package com.ustadmobile.nanolrs.ormlite.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.ustadmobile.nanolrs.core.model.XapiDocument;

/**
 * Created by mike on 10/2/16.
 */

@DatabaseTable(tableName = "xapi_document")
public class XapiDocumentEntity implements XapiDocument {

    @DatabaseField(id  = true)
    private String id;

    @DatabaseField
    private String contentType;

    @DatabaseField(dataType =  DataType.LONG_STRING)
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }






}
