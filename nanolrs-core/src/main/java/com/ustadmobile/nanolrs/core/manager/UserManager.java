package com.ustadmobile.nanolrs.core.manager;

import com.ustadmobile.nanolrs.core.model.User;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by mike on 9/27/16.
 */

public interface UserManager extends NanoLrsManagerSyncable {

    /**
     *
     * @param dbContext
     * @param id
     * @return
     */
    User createSync(Object dbContext, String id);

    /**
     * Find users by id .
     * @param dbContext Databse context
     * @param id ID field
     * @return
     */
    User findById(Object dbContext, String id);

    /**
     * Find users by username (pk)
     * @param dbContext Database context
     * @param username  Username (pk)
     * @return
     */
    User findByUsername(Object dbContext, String username);

    /**
     * Deletes the user. Not finished.
     * @param dbContext
     * @param data
     */
    void delete(Object dbContext, User data);

    /**
     * Authenticate locally the user with given username, password
     * @param dbContext Database Context
     * @param username  User's username (pk)
     * @param password  User's password to check against.
     * @return true if success, false if fail
     */
    boolean authenticate(Object dbContext, String username, String password);

    /**
     * Authenticate locally the user with given username and password (hashed or not)
     * @param dbContext
     * @param username
     * @param password
     * @param hashit    If password should be hashed before authenticating
     * @return
     */
    boolean authenticate(Object dbContext, String username, String password, boolean hashit);

    /**
     * Save password in user table as a hash
     * @param password  User's password in plain text
     * @param dbContext Database context
     * @return  true if password update a success, fail if not.
     */
    User updatePassword(String password, User user, Object dbContext)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, SQLException ;

    /**
     * Updates the username for an existing user and updates the Xapi_Agent mapping as well.
     * @param username
     * @param user
     * @param dbContext
     * @return
     * @throws SQLException
     */
    boolean updateUsername(String username, User user, Object dbContext)
        throws SQLException;

    /**
     * Hash's password. Returns hashed password. Simple stuff.
     * @param password
     * @return
     */
    String hashPassword(String password)
            throws UnsupportedEncodingException, NoSuchAlgorithmException;

}
