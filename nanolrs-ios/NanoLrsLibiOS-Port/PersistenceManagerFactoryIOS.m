//
//  PersistenceManagerFactoryIOS.m
//  NanoLrsLibiOS
//
//  Created by Mike Dawson on 05/02/2017.
//  Copyright © 2017 UstadMobile FZ-LLC. All rights reserved.
//

#import "PersistenceManagerFactoryIOS.h"


@implementation PersistenceManagerFactoryIOS

-(ComUstadmobileNanolrsCorePersistencePersistenceManager *)getPersistenceManager {
    return [[PersistenceManagerIOS alloc]init];
}


@end
