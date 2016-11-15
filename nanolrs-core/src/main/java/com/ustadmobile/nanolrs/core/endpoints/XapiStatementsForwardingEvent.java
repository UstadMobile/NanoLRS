package com.ustadmobile.nanolrs.core.endpoints;

import com.ustadmobile.nanolrs.core.model.XapiStatementProxy;

/**
 * Event describing a change in the status of the Xapi Statements Queue
 * Very basic at the moment
 *
 * Created by mike on 11/11/16.
 */

public class XapiStatementsForwardingEvent {

    private int statementsRemaining;

    private XapiStatementProxy statement;

    /**
     * Constructor: include the number of statements currently remaining
     *
     * @param statementsRemaining
     */
    public XapiStatementsForwardingEvent(int statementsRemaining) {
        this.statementsRemaining = statementsRemaining;
    }

    public XapiStatementsForwardingEvent(XapiStatementProxy statement) {
        this.statement = statement;
    }

    public XapiStatementProxy getStatement() {
        return statement;
    }

    /**
     * Gets the number of statements left in the Queue
     *
     * @return Number of unsent statements left in the queue to send
     */
    public int getStatementsRemaining() {
        return statementsRemaining;
    }


}
