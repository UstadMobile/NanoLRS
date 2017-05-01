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

}
