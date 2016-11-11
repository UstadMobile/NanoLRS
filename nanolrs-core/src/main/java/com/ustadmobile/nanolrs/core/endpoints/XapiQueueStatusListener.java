package com.ustadmobile.nanolrs.core.endpoints;

/**
 * Interface to listen for updates about the xapi statement queue
 */
public interface XapiQueueStatusListener {

    /**
     * Called when the queue has been updated: item(s) have been added or successfully transmitted
     *
     * @param event Event with the Queue status itself
     */
    void queueStatusUpdated(XapiQueueStatusEvent event);

}
