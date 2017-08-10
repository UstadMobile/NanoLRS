package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.ormlite.generated.model.UserEntity;

import java.sql.SQLException;
import java.util.List;

/*
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
*/

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
        //String givenUserUUID = user.getUuid();
        String newUsername = null;

        //User existingUser = (User)findByPrimaryKey(dbContext, givenUserUUID);
        User existingUser = (User)findByUsername(dbContext, givenUsername);
        if(existingUser == null){
            //Likely a new user creation not an update
            //List<User> usersWithSameUsername = findByUsername(dbContext, givenUsername);
            //if(usersWithSameUsername != null && !usersWithSameUsername.isEmpty()){
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
            //TODO: Tick off this edge case .
            //Scenario: Maybe the username in agent changed by mistake
            /*
            List<User> usersWithSameUsername = findByUsername(dbContext, givenUsername);
            if(usersWithSameUsername != null && !usersWithSameUsername.isEmpty()){
                newUsername = givenUsername + (int)Math.floor(Math.random() * 101);
                ((User) data).setUsername(newUsername);
                //Since we changed the username. we persist again to bump local seq
                super.persist(dbContext, data);
            */
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
        //QueryBuilder<UserEntity, String> subQueryQBColumn = subQueryQB.selectColumns("uuid");
        QueryBuilder<UserEntity, String> subQueryQBColumn = subQueryQB.selectColumns("username");
        Where subQueryColumnWhere = subQueryQBColumn.where();
        //subQueryColumnWhere.eq(UserEntity.COLNAME_UUID, user.getUuid());
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
            /*
            QueryBuilder<UserEntity, String> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(UserEntity.COLNAME_USERNAME, username);
            return (List<User>)(Object)dao.query(queryBuilder.prepare());
            */
            return dao.queryForId(username);
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
    public boolean authenticate(Object dbContext, String username, String password) {
        /*
        List<User> users = findByUsername(dbContext, username);
        if(users == null || users.size() == 0){
            return false;
        }
        User user = users.get(0);
        */
        //TODO: hash password
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

    /*
    public class AeSimpleSHA1 {

        private String convertToHex(byte[] data) {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < data.length; i++) {
                int halfbyte = (data[i] >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    if ((0 <= halfbyte) && (halfbyte <= 9))
                        buf.append((char) ('0' + halfbyte));
                    else
                        buf.append((char) ('a' + (halfbyte - 10)));
                    halfbyte = data[i] & 0x0F;
                } while (two_halfs++ < 1);
            }
            return buf.toString();
        }

        public String SHA1(String text)
                throws NoSuchAlgorithmException, UnsupportedEncodingException {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-1");
            byte[] sha1hash = new byte[40];
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            sha1hash = md.digest();
            return convertToHex(sha1hash);
        }
    }
    */
}
