package com.ustadmobile.nanolrs.core.manager;


import com.ustadmobile.nanolrs.core.model.NanoLrsModel;

import java.sql.SQLException;

/**
 * Base class for entity management.
 *
 * Created by mike on 4/30/17.
 */

public interface NanoLrsManager<T extends NanoLrsModel, P> {

    /**
     * Create a new object. This does *NOT* persist the entity.
     *
     * @return blank new object
     */
    T makeNew() throws SQLException;

    /**
     * Persist the object to the database
     *
     * @param data object to be persisted
     */
    void persist(Object dbContext, T data) throws SQLException;

    /*
    Trying persist with manager, so we can get properties of the table
    eg: latest field value, etc
     */
    void persist(Object dbContext, T data, NanoLrsManager manager) throws SQLException;

    /**
     * Ddelete the object from the database
     *
     * @param data
     */
    void delete(Object dbContext, T data) throws SQLException;

    /**
     * Find an object by it's primary key. Returns null if no such object exists.
     *
     * @param primaryKey Primary key value to search for
     *
     * @return Object if found, otherwise null
     */
    T findByPrimaryKey(Object dbContext, P primaryKey) throws SQLException;

    /*
    Gets latest local sequence of this table
     */
    long getLatestLocalSequence(Object dbContext) throws SQLException;

    /*
    Gets latest master sequence of this table
     */
    long getLatestMasterSequence(Object dbContext) throws SQLException;

}
