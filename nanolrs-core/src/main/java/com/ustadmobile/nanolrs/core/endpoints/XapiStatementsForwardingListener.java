package com.ustadmobile.nanolrs.core.endpoints;

/**
 * Interface to listen for updates about the xapi statement queue
 */
public interface XapiStatementsForwardingListener {

    /**
     * Called when the queue has been updated: item(s) have been added or successfully transmitted
     *
     * @param event Event with the Queue status itself
     */
    void queueStatusUpdated(XapiStatementsForwardingEvent event);

    /**
     * Called each time a statement has been successfully
     * @param event
     */
    void queueStatementSent(XapiStatementsForwardingEvent event);

    /**
     * Called when a statement has been added to the Queue
     *
     * @param event
     */
    void statementQueued(XapiStatementsForwardingEvent event);


}
