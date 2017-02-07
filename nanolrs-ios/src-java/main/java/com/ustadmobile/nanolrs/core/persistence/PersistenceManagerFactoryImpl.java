package com.ustadmobile.nanolrs.core.persistence;

/*-[
#import "PersistenceManagerIOS.h"
]-*/

/**
 * Created by mike on 2/7/17.
 */

public class PersistenceManagerFactoryImpl {

    public static native PersistenceManager getPersistenceManager() /*-[
    	return [[PersistenceManagerIOS alloc]init];
    ]-*/;

}
