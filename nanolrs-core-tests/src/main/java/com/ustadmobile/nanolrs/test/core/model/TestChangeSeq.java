package com.ustadmobile.nanolrs.test.core.model;
/**
 * Created by varuna on 6/23/2017.
 */

import com.ustadmobile.nanolrs.core.manager.ChangeSeqManager;
import com.ustadmobile.nanolrs.core.model.ChangeSeq;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.test.core.NanoLrsPlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class TestChangeSeq {
    @Test
    public void testLifecycle() throws Exception {
        //Get the connectionSource from platform db pool (from NanoLrsPlatformTestUtil)
        Object context = NanoLrsPlatformTestUtil.getContext();
        String tableName;
        long nextSeqNum;

        //Create a changeSeq entry for this test:
        ChangeSeqManager changeSeqManager = PersistenceManager.getInstance().getManager(
                ChangeSeqManager.class);
        tableName = "RELATIONSHIP_TEST";
        nextSeqNum = 42;
        ChangeSeq changeSeq = (ChangeSeq) changeSeqManager.makeNew();
        changeSeq.setUUID(UUID.randomUUID().toString());
        changeSeq.setTable(tableName);
        changeSeq.setNextChangeSeqNum(nextSeqNum);
        changeSeqManager.persist(context, changeSeq, changeSeqManager);

        long gottenNextSeqNum = changeSeqManager.getNextChangeByTableName(tableName, context);
        Assert.assertEquals(gottenNextSeqNum, 42);


        changeSeqManager.getNextChangeAddSeqByTableName(tableName, 2, context);

        long postIncrementGottenNextSeqNumber =
                changeSeqManager.getNextChangeByTableName(tableName, context);
        Assert.assertEquals(postIncrementGottenNextSeqNumber, 44);

    }
}
