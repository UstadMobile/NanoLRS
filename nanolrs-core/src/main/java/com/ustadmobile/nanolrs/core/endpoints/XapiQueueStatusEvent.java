package com.ustadmobile.nanolrs.core.endpoints;

/**
 * Event describing a change in the status of the Xapi Statements Queue
 * Very basic at the moment
 *
 * Created by mike on 11/11/16.
 */

public class XapiQueueStatusEvent {

    private int statementsRemaining;

    /**
     * Constructor: include the number of statements currently remaining
     *
     * @param statementsRemaining
     */
    public XapiQueueStatusEvent(int statementsRemaining) {
        this.statementsRemaining = statementsRemaining;
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
