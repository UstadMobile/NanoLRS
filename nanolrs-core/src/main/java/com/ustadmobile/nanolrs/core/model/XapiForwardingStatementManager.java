package com.ustadmobile.nanolrs.core.model;

import java.util.List;

/**
 * Created by mike on 9/13/16.
 */
public interface XapiForwardingStatementManager {

    XapiForwardingStatementProxy createSync(Object dbContext, String uuid);

    List<XapiForwardingStatementProxy> getAllUnsentStatementsSync(Object dbContext);

    void persistSync(Object dbContext, XapiForwardingStatementProxy forwardingStatement);

    XapiForwardingStatementProxy findByUuidSync(Object dbContext, String uuid);

    int getUnsentStatementCount(Object dbContext);

    /**
     * Get the queue status of the given statement
     *
     * @param dbContext
     * @param statement
     * @return
     */
    int findStatusByXapiStatement(Object dbContext, XapiStatementProxy statement);

}
