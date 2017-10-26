package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.NanoLrsModelSyncable;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;
import com.ustadmobile.nanolrs.core.util.AeSimpleSHA1;
import com.ustadmobile.nanolrs.core.util.DjangoHasher;
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
        persist(dbContext, data, true);
    }

    //@Override
    public void persist(Object dbContext, NanoLrsModel data, boolean incrementChangeSeq) throws SQLException {
        //Check username
        User user = (User) data;
        String givenUsername = user.getUsername();
        String newUsername = null;

        User existingUser = findByUsername(dbContext, givenUsername);
        if(existingUser == null){
            //Likely a new user creation not an update
            System.out.println("UserManager: New user creation.");
            User usersWithSameUsername = findByUsername(dbContext, givenUsername);

            //The below code will never run since were checking above. TODO: Check and remove.
            if(usersWithSameUsername != null){
                System.out.println("UserManager: PLEASE CHECK. THIS SHOULD NOT HAPPEN." +
                        "For user: (" + existingUser.getUsername() + " / "
                            + usersWithSameUsername.getUsername() +").");
                newUsername = givenUsername + (int)Math.floor(Math.random() * 101);
                ((User) data).setUsername(newUsername);

                //Since we changed the username. we persist again to bump local seq
                //That way it goes back to other nodes.
                super.persist(dbContext, data, incrementChangeSeq);

            }else{
                //Added: Setting master -1 for new Users
                System.out.println("UserManager: Master set to -1 for new users.");
                ( (User)data).setMasterSequence(-1);
            }
        }else{
            //If an update, it is probably a mistake. We should ignore this push
            System.out.println("UserManager: User: (" + givenUsername +
                    ") getting an update.");
        }

        if(newUsername != null){
            givenUsername = newUsername;
        }

        System.out.println("UserManager: Updating password for : " + givenUsername);
        String userPassword = ((User) data).getPassword();
        System.out.println("    password: " + userPassword + "");
        if(userPassword != null){
            if(!userPassword.isEmpty()) {
                if (!userPassword.startsWith("pbkdf2_sha256")) {
                    String username = "";
                    try {
                        username = ((User) data).getUsername();
                    } catch (Exception e) {
                        System.out.println("UserManager: Getting username exception. " + e);
                    }
                    System.out.println(" UserManager: User password (user:" + username + ") coming " +
                            "is in clear text. Hashing it..");
                    DjangoHasher dh = new DjangoHasher();
                    String hashedPassword = dh.encode(userPassword);
                    if (hashedPassword != null && !hashedPassword.isEmpty()) {
                        ((User) data).setPassword(hashedPassword);
                    }
                }
            }
        }
        System.out.println(" UserManager: custom persist done.");

        super.persist(dbContext, data, incrementChangeSeq);
        System.out.println(" UserManager: super persist done.");
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
     * TODO: Don't delete, set active=False.
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
        return authenticate(dbContext, username, password, false);
    }

    @Override
    public boolean authenticate(Object dbContext, String username, String password, boolean hashIt) {

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
        String checkThisPassword = password;

        if(hashIt){

            DjangoHasher hasher = new DjangoHasher();

            boolean dHasherCheck = hasher.checkPassword(checkThisPassword, user.getPassword());
            if(dHasherCheck){
                return true;
            }

            try {
                checkThisPassword = hashPassword(password);
            } catch (UnsupportedEncodingException e) {
                System.out.println("Cannot hash password in authenticate");
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Cannot hash password in authenticate..");
                e.printStackTrace();
            }
        }

        if(user.getPassword().equals(checkThisPassword)){
            return true;
        }else{
            System.out.println("User: " + username + " authentication FAILED.");
            return false;
        }
    }

    /**
     * Update password (text -> hash) for already existing user. Includes checks, and persists.
     *
     * @param password The password in plain text form.
     * @param dbContext Database context
     * @return  user with updated hashed password. null if password is empty or null.
     */
    @Override
    public User updatePassword(String password, User user, Object dbContext)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, SQLException {
        if(password != null && !password.isEmpty()){
            String hashPassword = hashPassword(password);
            user.setPassword(hashPassword);
            persist(dbContext, user);
            return user;
        }
        return null;
    }

    /**
     * Hash's password. Simple stuff. Just returns the value.
     *
     * @param password
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    @Override
    public String hashPassword(String password)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        //return AeSimpleSHA1.SHA1(password);
        DjangoHasher hasher = new DjangoHasher();
        String hashedPassword = hasher.encode(password);
        return hashedPassword;
    }

    @Override
    public boolean updateUsername(String newUsername, User user, Object dbContext)
            throws SQLException {

        XapiAgentManager agentManager =
                PersistenceManager.getInstance().getManager(XapiAgentManager.class);
        user.setUsername(newUsername);
        persist(dbContext, user);

        //The updated user.
        User newUser = findByUsername(dbContext, newUsername);

        //Update the agent (All xapi tables depend on this)
        List<XapiAgent> usersAgents = agentManager.findByUser(dbContext, user);
        XapiAgent usersAgent = usersAgents.get(0);

        usersAgent.setUser(newUser);
        agentManager.persist(dbContext, usersAgent); //We should +1 LS since its an update.

        return true;
    }


}
