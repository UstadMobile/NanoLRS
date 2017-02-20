//
//  XapiForwardingStatementManager.m
//  NanoLrsLibiOS
//
//  Created by Mike Dawson on 10/02/2017.
//  Copyright © 2017 UstadMobile FZ-LLC. All rights reserved.
//

#import "XapiForwardingStatementManagerSharkORM.h"
#import "java/util/List.h"

@implementation XapiForwardingStatementManagerSharkORM


- (id<ComUstadmobileNanolrsCoreModelXapiForwardingStatement>)createSyncWithId:(id)dbContext
                                                                 withNSString:(NSString *)uuid {
    XapiForwardingStatementSrkObj *obj = [XapiForwardingStatementSrkObj new];
    [obj setUuidWithNSString:uuid];
    [obj commit];
    return obj;
}

-(SRKQuery *)makeAllUnsentStmtsQuery {
    SRKQuery *query = [[XapiForwardingStatementSrkObj query]where:[NSString stringWithFormat:@"_status < %ld", (long)ComUstadmobileNanolrsCoreModelXapiForwardingStatement_get_STATUS_SENT()]];
    return query;
}

- (id<JavaUtilList>)getAllUnsentStatementsSyncWithId:(id)dbContext {
    SRKQuery *query = [self makeAllUnsentStmtsQuery];
    return [self makeJavaUtilListWithSRKResultSet:[query fetch] withClassType:ComUstadmobileNanolrsCoreModelXapiForwardingStatement_class_()];
}

- (void)persistSyncWithId:(id)dbContext
withComUstadmobileNanolrsCoreModelXapiForwardingStatement:(id<ComUstadmobileNanolrsCoreModelXapiForwardingStatement>)forwardingStatement {
    XapiForwardingStatementSrkObj *obj = (XapiForwardingStatementSrkObj *)forwardingStatement;
    [obj commit];
}

- (id<ComUstadmobileNanolrsCoreModelXapiForwardingStatement>)findByUuidSyncWithId:(id)dbContext
                                                                     withNSString:(NSString *)uuid {
    ;
    SRKResultSet *result = [[[XapiForwardingStatementSrkObj query]whereWithFormat:@"Id = %@" withParameters:@[uuid]]fetch];
    return [self returnFirstResultWithSRKResultSet:result];
}

- (jint)getUnsentStatementCountWithId:(id)dbContext {
    return (jint)[[[self makeAllUnsentStmtsQuery]fetch]count];
}

- (jint)findStatusByXapiStatementWithId:(id)dbContext
withComUstadmobileNanolrsCoreModelXapiStatement:(id<ComUstadmobileNanolrsCoreModelXapiStatement>)statement {
    SRKQuery *query = [[[XapiForwardingStatementSrkObj query]whereWithFormat:@"_statement = %@" withParameters:@[[statement getUuid]]]limit:1];
    SRKResultSet *result = [query fetch];
    if([result count] > 0) {
        XapiForwardingStatementSrkObj *obj = [result objectAtIndex:0];
        return obj._status;
    }else {
        return -2;//As per ORMLite implementation - this should be flaged
    }
}

@end
