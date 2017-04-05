package com.ustadmobile.nanolrs.jdbc.persistence;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiActivityEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiAgentEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiDocumentEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiForwardingStatementEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStateEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiStatementEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiUserEntity;
import com.ustadmobile.nanolrs.ormlite.generated.model.XapiVerbEntity;
import com.ustadmobile.nanolrs.ormlite.persistence.PersistenceManagerORMLite;

import java.sql.SQLException;

/**
 * Created by Varuna on 4/4/2017.
 */
public class PersistenceManagerJDBC extends PersistenceManagerORMLite {

    //Testing:
    private final static String DATABASE_URL = "jdbc:postgresql://localhost:5432/test?user=postgres&password=varuna";

    ConnectionSource connectionSource;

    @Override
    public <D extends Dao<T, ?>, T> D getDao(Class<T> clazz, Object dbContext) throws SQLException {
        dbSync();
        return DaoManager.createDao(this.connectionSource, clazz);
    }

    /**
     * Sync the Database: Connect to the database,
     * Create new tables and relations if required.
     */
    public void dbSync() throws IllegalArgumentException{
        System.out.println("hi");
        //ConnectionSource connectionSource = null;
        try{
            //Create our Data-source for the database
            this.connectionSource = new JdbcConnectionSource(DATABASE_URL);

            //Check if database needs sync- figure out how
            //Set up out database and DAOs
            //setupDatabase(connectionSource);
            createTables();

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println( "DB Sync Sql Exception! " + e );
            throw new IllegalArgumentException("SQL Exception: " + e);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println( "DB Sync Sql Exception! " + e );
            throw new IllegalArgumentException("Exception : " + e);

        } finally {
            //Clean up block. So we don't usually return here.

        }
        System.out.println("DB Sync OK");
    }

    public static Class[] TABLE_CLASSES = new Class[]{ XapiActivityEntity.class, XapiAgentEntity.class,
            XapiStatementEntity.class, XapiVerbEntity.class, XapiForwardingStatementEntity.class,
            XapiUserEntity.class, XapiDocumentEntity.class, XapiStateEntity.class
    };

    public void createTables() {
        try {
            System.out.println("onCreate");
            for(Class clazz : TABLE_CLASSES) {
                TableUtils.createTableIfNotExists(this.connectionSource, clazz);
                //TableUtils.createTable(this.connectionSource, clazz);
            }
        }catch(SQLException e) {
            System.out.println("can't create database");
            throw new RuntimeException(e);
        }
    }

    //@Override
    public <D extends Dao<T, ?>, T> D getDao2(Class<T> clazz, Object dbContext) throws SQLException {
        //return null;
        dbSync();
        //eg:
        //Dao<AnEntity, Integer> anentityDao;
        //anentityDao = DaoManager.createDao(connectionSource, AnEntity.class);
        return DaoManager.createDao(this.connectionSource, clazz);
    }
}
