//
//  XapiStatementManagerSharkORM.m
//  NanoLrsLibiOS
//
//  Created by Mike Dawson on 08/02/2017.
//  Copyright © 2017 UstadMobile FZ-LLC. All rights reserved.
//

#import "XapiStatementManagerSharkORM.h"
#import "XapiStatementSrkObj.h"
#import "java/util/List.h"
#import "PersistenceReceiver.h"
#import "java/util/UUID.h"

@implementation XapiStatementManagerSharkORM

- (void)findByUuidWithId:(id)dbContext
                 withInt:(jint)requestId
withComUstadmobileNanolrsCorePersistencePersistenceReceiver:(id<ComUstadmobileNanolrsCorePersistencePersistenceReceiver>)receiver
            withNSString:(NSString *)uuid {
    id<ComUstadmobileNanolrsCoreModelXapiStatement> obj = [self findByUuidSyncWithId:dbContext withNSString:uuid];
    if(obj != nil) {
        [receiver onPersistenceSuccessWithId:obj withInt:requestId];
    }else {
        [receiver onPersistenceFailureWithId:nil withInt:requestId];
    }
}

- (id<JavaUtilList>)findByParamsWithId:(id)dbContext
                          withNSString:(NSString *)statementid
                          withNSString:(NSString *)voidedStatemendid
withComUstadmobileNanolrsCoreModelXapiAgent:(id<ComUstadmobileNanolrsCoreModelXapiAgent>)agent
                          withNSString:(NSString *)verb
                          withNSString:(NSString *)activity
                          withNSString:(NSString *)registration
                           withBoolean:(jboolean)relatedActivities
                           withBoolean:(jboolean)relatedAgents
                              withLong:(jlong)since
                              withLong:(jlong)until
                               withInt:(jint)limit {
    BOOL whereHasClauses = NO;
    NSMutableString *whereStr = [[NSMutableString alloc]init];
    SRKQuery *query = [XapiStatementSrkObj query];
    
    if(statementid != nil) {
        [whereStr appendFormat:@"Id = \"%@\"", statementid];
        whereHasClauses = YES;
    }
    
    if(agent != nil) {
        if(whereHasClauses)
            [whereStr appendString:@" AND "];
        [whereStr appendFormat:@" _agent = \"%@\" ", [agent getUuid]];
        whereHasClauses = YES;
    }
    
    if(verb != nil) {
        if(whereHasClauses)
            [whereStr appendString:@" AND "];
        [whereStr appendFormat:@" _verb = \"%@\" ", verb];
        whereHasClauses = YES;
    }
    
    if(activity != nil) {
        if(whereHasClauses)
            [whereStr appendString:@" AND "];
        [whereStr appendFormat:@" _activity = \"%@\" ", activity];
        whereHasClauses = YES;
    }
    
    if(registration != nil) {
        if(whereHasClauses)
            [whereStr appendString:@" AND "];
        [whereStr appendFormat:@" _contextRegistration = \"%@\" ", registration];
        whereHasClauses = YES;
    }
    
    
    if(since >= 0) {
        if(whereHasClauses)
            [whereStr appendString:@" AND "];
        
        [whereStr appendFormat:@" _timestamp > %ld", (long)since];
        whereHasClauses = YES;
    }
    
    if(until >= 0) {
        if(whereHasClauses)
            [whereStr appendString:@" AND "];
        
        [whereStr appendFormat:@" _timestamp < %ld", (long)until];
        whereHasClauses = YES;
    }
    
    if(limit > 0) {
        query = [query limit:limit];
    }
    
    query = [query orderBy:@"_timestamp"];
    
    return [self makeJavaUtilListWithSRKResultSet:[query fetch] withClassType:ComUstadmobileNanolrsCoreModelXapiStatement_class_()];
}

- (id<ComUstadmobileNanolrsCoreModelXapiStatement>)findByUuidSyncWithId:(id)dbContext
                                                           withNSString:(NSString *)uuid {
    SRKResultSet *result = [[[[XapiStatementSrkObj query]whereWithFormat:@"Id = %@" withParameters:@[uuid]]limit:1]fetch];
    return [self returnFirstResultWithSRKResultSet:result];
}

- (void)createWithId:(id)dbContext
             withInt:(jint)requestId
withComUstadmobileNanolrsCorePersistencePersistenceReceiver:(id<ComUstadmobileNanolrsCorePersistencePersistenceReceiver>)receiver {
    [receiver onPersistenceSuccessWithId:[self createSyncWithId:dbContext] withInt:requestId];
}

- (id<ComUstadmobileNanolrsCoreModelXapiStatement>)createSyncWithId:(id)dbContext {
    XapiStatementSrkObj *stmt = [XapiStatementSrkObj new];
    [stmt setUuidWithNSString:[[JavaUtilUUID randomUUID]description]];
    [stmt commit];
    return stmt;
}

- (void)persistSyncWithId:(id)dbContext
withComUstadmobileNanolrsCoreModelXapiStatement:(id<ComUstadmobileNanolrsCoreModelXapiStatement>)stmt {
    XapiStatementSrkObj *stmtSrkObj = (XapiStatementSrkObj *)stmt;
    [stmtSrkObj commit];
}


@end
