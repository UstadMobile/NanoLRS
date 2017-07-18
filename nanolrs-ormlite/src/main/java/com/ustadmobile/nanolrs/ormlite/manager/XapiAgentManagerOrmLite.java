package com.ustadmobile.nanolrs.ormlite.manager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.ustadmobile.nanolrs.core.manager.XapiAgentManager;
import com.ustadmobile.nanolrs.core.model.NanoLrsModel;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.model.XapiAgent;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiAgentEntity;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by mike on 10/6/16.
 */

public class XapiAgentManagerOrmLite extends BaseManagerOrmLite  implements XapiAgentManager{

    public XapiAgentManagerOrmLite() {
    }

    @Override
    public Class getEntityImplementationClasss() {
        return XapiAgentEntity.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<XapiAgent> findAgentByParams(Object dbContext, String mbox, String accountName, String accountHomepage) {
        if(mbox == null &&(accountName == null || accountHomepage == null)) {
            throw new IllegalArgumentException("findAgentByParams MUST have at least mbox or accountName and accountHomepage");
        }

        try {
            Dao<XapiAgentEntity, String> dao = persistenceManager.getDao(XapiAgentEntity.class, dbContext);
            QueryBuilder<XapiAgentEntity, String> queryBuilder = makeAgentQuery(dao, mbox, accountName, accountHomepage);
            List<XapiAgentEntity> results = dao.query(queryBuilder.prepare());
            return (List<XapiAgent>)(Object)results;
        }catch(SQLException e) {
            System.err.println("Exception in findAgentByParams");
            e.printStackTrace();
        }

        return null;
    }

    protected static QueryBuilder<XapiAgentEntity, String> makeAgentQuery(Dao<XapiAgentEntity, String> dao, String mbox, String accountName, String accountHomepage) throws SQLException{
        QueryBuilder<XapiAgentEntity, String> queryBuilder = dao.queryBuilder();
        Where<XapiAgentEntity, String> where = queryBuilder.where();

        if(mbox != null) {
            where.eq(XapiAgentEntity.COLNAME_MBOX, mbox);
        }

        if(accountName != null && accountHomepage != null) {
            if(mbox != null)
                where.and();

            where.and(
                    where.eq(XapiAgentEntity.COLNAME_ACCOUNT_NAME, accountName),
                    where.eq(XapiAgentEntity.COLNAME_ACCOUNT_HOMEPAGE, accountHomepage)
            );
        }

        return queryBuilder;
    }

    @Override
    public XapiAgent makeNew(Object dbContext) {
        return new XapiAgentEntity();
    }

    @Override
    public NanoLrsModel findAllRelatedToUser(Object dbContext, User user) {
        return null;
    }

    @Override
    public PreparedQuery findAllRelatedToUserQuery(Object dbContext, User user) {
        return null;
    }

    @Override
    public void createOrUpdate(Object dbContext, XapiAgent data) {
        try {
            Dao<XapiAgentEntity, String> dao = persistenceManager.getDao(XapiAgentEntity.class, dbContext);
            dao.createOrUpdate((XapiAgentEntity) data);
        } catch (SQLException e) {
            System.err.println("Exception agent manager createOrUpdate");
            e.printStackTrace();
        }
    }

    /*
    @Override
    public NanoLrsModel makeNew() throws SQLException {
        return null;
    }
    */
}
