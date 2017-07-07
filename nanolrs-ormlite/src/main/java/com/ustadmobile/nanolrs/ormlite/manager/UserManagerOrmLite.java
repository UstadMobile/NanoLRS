package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.ormlite.generated.model.UserEntity;

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
    public NanoLrsModelSyncable findAllRelatedToUser(Object dbContext, User user) {
        return null;
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
    public List<User> findByUsername(Object dbContext, String username) {
        try {
            Dao<UserEntity, String> dao = persistenceManager.getDao(UserEntity.class, dbContext);
            QueryBuilder<UserEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(UserEntity.COLNAME_USERNAME, username);
            return (List<User>)(Object)dao.query(queryBuilder.prepare());
        }catch(Exception e) {
            System.err.println("Exception findByUsername");
            e.printStackTrace();
        }

        return null;
    }

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
    public List<User> getAll(Object dbContext) throws SQLException{
        Dao thisDao = persistenceManager.getDao(UserEntity.class, dbContext);
        QueryBuilder<UserEntity, String> qb = thisDao.queryBuilder();
        List<User> allEntities = thisDao.query(qb.prepare());
        return allEntities;

    }
}
