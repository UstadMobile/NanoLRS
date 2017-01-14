package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.XapiForwardingStatement;
import com.ustadmobile.nanolrs.core.model.XapiStatement;

import java.util.List;

/**
 * Created by mike on 9/13/16.
 */
public interface XapiForwardingStatementManager {

    XapiForwardingStatement createSync(Object dbContext, String uuid);

    List<XapiForwardingStatement> getAllUnsentStatementsSync(Object dbContext);

    void persistSync(Object dbContext, XapiForwardingStatement forwardingStatement);

    XapiForwardingStatement findByUuidSync(Object dbContext, String uuid);

    int getUnsentStatementCount(Object dbContext);

    /**
     * Get the queue status of the given statement
     *
     * @param dbContext
     * @param statement
     * @return
     */
    int findStatusByXapiStatement(Object dbContext, XapiStatement statement);

}
