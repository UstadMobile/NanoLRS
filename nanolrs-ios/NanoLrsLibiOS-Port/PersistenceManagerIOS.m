//
//  PersistenceManagerIOS.m
//  NanoLrsLibiOS
//
//  Created by Mike Dawson on 05/02/2017.
//  Copyright © 2017 UstadMobile FZ-LLC. All rights reserved.
//

#import "PersistenceManagerIOS.h"
#import "XapiActivityManagerSharkORM.h"
#import "XapiAgentManagerSharkORM.h"

@interface PersistenceManagerIOS()
@property XapiActivityManagerSharkORM *activityManager;
@property XapiAgentManagerSharkORM *agentManager;
@end

@implementation PersistenceManagerIOS

-(instancetype)init {
    self = [super init];
    self.activityManager = [[XapiActivityManagerSharkORM alloc]init];
    self.agentManager = [[XapiAgentManagerSharkORM alloc]init];
    return self;
}


- (id<ComUstadmobileNanolrsCoreManagerXapiActivityManager>)getActivityManager {
    return self.activityManager;
}

- (id<ComUstadmobileNanolrsCoreManagerXapiAgentManager>)getAgentManager {
    return self.agentManager;
}

- (id<ComUstadmobileNanolrsCoreManagerXapiForwardingStatementManager>)getForwardingStatementManager {
    return nil;
}

- (id<ComUstadmobileNanolrsCoreManagerXapiStateManager>)getStateManager {
    return nil;
}

- (id<ComUstadmobileNanolrsCoreManagerXapiStatementManager>)getStatementManager {
    return nil;
}

- (id<ComUstadmobileNanolrsCoreManagerXapiUserManager>)getUserManager {
    return nil;
}

- (id<ComUstadmobileNanolrsCoreManagerXapiVerbManager>)getVerbManager {
    return nil;
}


@end
