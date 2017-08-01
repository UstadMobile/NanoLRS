package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.UserCustomFieldsManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.UserCustomFields;
import com.ustadmobile.nanolrs.ormlite.generated.model.UserCustomFieldsEntity;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by varuna on 7/26/2017.
 */

public class UserCustomFieldsManagerOrmLite extends BaseManagerOrmLiteSyncable
        implements UserCustomFieldsManager {


    public UserCustomFieldsManagerOrmLite() {
    }

    @Override
    public Class getEntityImplementationClasss() {
        return UserCustomFieldsEntity.class;
    }

    @Override
    public List<NanoLrsModel> findAllRelatedToUser(Object dbContext, User user)
            throws SQLException {
        return null;
    }

    @Override
    public PreparedQuery<NanoLrsModel> findAllRelatedToUserQuery(Object dbContext, User user)
            throws SQLException {

        Dao thisDao = persistenceManager.getDao(UserCustomFieldsEntity.class, dbContext);
        QueryBuilder qb = thisDao.queryBuilder();
        QueryBuilder qbSelect = qb.selectColumns(UserCustomFieldsEntity.COLNAME_UUID);
        Where where = qbSelect.where();
        where.eq(UserCustomFieldsEntity.COLNAME_USER, user.getUuid());
        PreparedQuery pq = qbSelect.prepare();
        List<UserCustomFields> ucfs = thisDao.query(qbSelect.prepare());

        return pq;
    }

    @Override
    public void createUserCustom(Map<Integer, String> map, User user, Object dbContext)
            throws SQLException {
        //Dao thisDao = persistenceManager.getDao(NodeEntity.class, dbContext);

        Set<Map.Entry<Integer, String>> es = map.entrySet();
        Iterator<Map.Entry<Integer, String>> it = es.iterator();

        while(it.hasNext()){
            Map.Entry<Integer, String> e = it.next();
            int key = e.getKey();
            String value = e.getValue();

            UserCustomFields uce = (UserCustomFields) makeNew();
            uce.setUuid(UUID.randomUUID().toString());
            if(user!=null){
                uce.setUser(user);
            }
            uce.setFieldName(key);
            uce.setFieldValue(value);

            persist(dbContext, uce);
        }
    }

    @Override
    public List<UserCustomFields> findByUser(User user, Object dbContext) throws SQLException {

        Dao thisDao = persistenceManager.getDao(UserCustomFieldsEntity.class, dbContext);
        QueryBuilder qb = thisDao.queryBuilder();
        Where where = qb.where();
        where.eq(UserCustomFieldsEntity.COLNAME_USER, user.getUuid());
        List<UserCustomFields> ucfs = thisDao.query(qb.prepare());
        return ucfs;
    }

    @Override
    public String getUserField(User user, int field, Object dbContext) throws SQLException {
        Dao thisDao = persistenceManager.getDao(UserCustomFieldsEntity.class, dbContext);
        QueryBuilder qb = thisDao.queryBuilder();
        Where where = qb.where();
        where.eq(UserCustomFieldsEntity.COLNAME_USER, user.getUuid())
        .and()
        .eq(UserCustomFieldsEntity.COLNAME_FIELD_NAME, field);
        List<UserCustomFields> ucfs = thisDao.query(qb.prepare());
        if(ucfs != null && !ucfs.isEmpty()){
            UserCustomFields ucf = ucfs.get(0);
            return ucf.getFieldValue();
        }else{
            return "";
        }

    }


}
