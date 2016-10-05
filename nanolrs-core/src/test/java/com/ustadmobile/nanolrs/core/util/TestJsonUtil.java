package com.ustadmobile.nanolrs.core.util;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Created by mike on 10/5/16.
 */
@RunWith(JUnit4.class)
public class TestJsonUtil {

    @Test
    public void testJsonMerge() throws Exception {
        JSONObject srcObj = new JSONObject();
        srcObj.put("answer", "42");

        JSONObject nestedObj = new JSONObject();
        nestedObj.put("key1", "value1");
        srcObj.put("extension", nestedObj);

        JSONObject dstObj = new JSONObject();
        JSONObject nestedObj2 = new JSONObject();
        nestedObj2.put("key2", "value2");
        dstObj.put("extension", nestedObj2);

        JsonUtil.mergeJson(srcObj, dstObj);
        Assert.assertEquals(dstObj.getJSONObject("extension").getString("key1"), "value1");
    }


}
