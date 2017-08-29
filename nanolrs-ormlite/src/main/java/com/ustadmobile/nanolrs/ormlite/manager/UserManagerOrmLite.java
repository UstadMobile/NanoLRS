package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.util.AeSimpleSHA1;
import com.ustadmobile.nanolrs.ormlite.generated.model.UserEntity;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;


/**
 * Created by mike on 9/27/16.
 */

public class UserManagerOrmLite extends BaseManagerOrmLiteSyncable implements UserManager {

    public UserManagerOrmLite() {
    }

    @Override
    public Class getEntityImplementationClasss() {
        return UserEntity.class;
    }

    @Override
    public List<NanoLrsModelSyncable> findAllRelatedToUser(Object dbContext, User user)
            throws SQLException{
        return null;
    }

    @Override
    public void persist(Object dbContext, NanoLrsModel data) throws SQLException {
        //Check username
        User user = (User) data;
        String givenUsername = user.getUsername();
        String newUsername = null;

        User existingUser = findByUsername(dbContext, givenUsername);
        if(existingUser == null){
            //Likely a new user creation not an update
            User usersWithSameUsername = findByUsername(dbContext, givenUsername);
            if(usersWithSameUsername != null){
                newUsername = givenUsername + (int)Math.floor(Math.random() * 101);
                ((User) data).setUsername(newUsername);

                //Since we changed the username. we persist again to bump local seq
                //That way it goes back to other nodes.
                super.persist(dbContext, data);
            }
        }else{
            //If an update, it is probably a mistake. We should ignore this push
        }

        super.persist(dbContext, data);

    }

    @Override
    public PreparedQuery findAllRelatedToUserQuery(Object dbContext, User user)
            throws SQLException{
        // Return all user entities realted to user.
        //You will only return this user as a prepared Query
        Dao<UserEntity, String> thisDao = persistenceManager.getDao(UserEntity.class, dbContext);

        QueryBuilder<UserEntity, String> subQueryQB = thisDao.queryBuilder();
        QueryBuilder<UserEntity, String> subQueryQBColumn = subQueryQB.selectColumns("username");
        Where subQueryColumnWhere = subQueryQBColumn.where();
        subQueryColumnWhere.eq(UserEntity.COLNAME_USERNAME, user.getUsername());
        PreparedQuery<UserEntity> subQueryColumnPQ = subQueryQBColumn.prepare();
        return subQueryColumnPQ;
    }

    @Override
    public User createSync(Object dbContext, String id) {
        UserEntity created = new UserEntity();
        created.setUuid(id);
        return created;
    }

    @Override
    public User findById(Object dbContext, String id) {
        try {
            Dao<UserEntity, String> dao = persistenceManager.getDao(UserEntity.class, dbContext);
            return dao.queryForId(id);
        }catch(SQLException e) {
            System.err.println("Exception findById");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public User findByUsername(Object dbContext, String username) {
        try {
            Dao<UserEntity, String> dao = persistenceManager.getDao(UserEntity.class, dbContext);
            return dao.queryForId(username);
        }catch(Exception e) {
            System.err.println("Exception findByUsername");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * TODO: Don't delete, set active=False
     * @param dbContext
     * @param data
     */
    @Override
    public void delete(Object dbContext, User data) {
        try {
            Dao<UserEntity, String> dao = persistenceManager.getDao(UserEntity.class, dbContext);
            dao.delete((UserEntity)data);
        }catch(SQLException e) {
            System.err.println("exception deleting");
            e.printStackTrace();
        }
    }

    @Override
    public boolean authenticate(Object dbContext, String username, String password) {
        User user = findByUsername(dbContext, username);
        if(user == null){
            return false;
        }
        if(user.getUsername() == null || password == null){
            return false;
        }
        if(user.getPassword() == null || password == null){
            return false;
        }
        if(user.getPassword().equals(password)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Save password in user as hash
     * @param password The password in plain text form.
     * @param dbContext Database context
     * @return
     */
    @Override
    public boolean updatePassword(String password, User user, Object dbContext)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, SQLException {
        if(password != null && !password.isEmpty()){
            String hashPassword = AeSimpleSHA1.SHA1(password);
            user.setPassword(hashPassword);
            persist(dbContext, user);
            return true;
        }
        return false;
    }


}
