//
//  XapiStateManagerSharkORM.m
//  NanoLrsLibiOS
//
//  Created by Mike Dawson on 08/02/2017.
//  Copyright © 2017 UstadMobile FZ-LLC. All rights reserved.
//

#import "XapiStateManagerSharkORM.h"
#import "XapiStateSrkObj.h"

@implementation XapiStateManagerSharkORM

- (id<ComUstadmobileNanolrsCoreModelXapiState>)makeNewWithId:(id)dbContext {
    return [XapiStateSrkObj new];
}

- (id<ComUstadmobileNanolrsCoreModelXapiState>)findByActivityAndAgentWithId:(id)dbContext
                                                               withNSString:(NSString *)activityId
                                                               withNSString:(NSString *)agentMbox
                                                               withNSString:(NSString *)agentAccountName
                                                               withNSString:(NSString *)agentAccountHomepage
                                                               withNSString:(NSString *)registrationUuid
                                                               withNSString:(NSString *)stateId {
    NSMutableString *whereStr = [[NSMutableString alloc]init];
    SRKQuery *query = [XapiStateSrkObj query];
    [whereStr appendFormat:@"_activity = \"%@\" AND _stateId = \"%@\" ", activityId, stateId];
    
    if(registrationUuid != nil) {
        [whereStr appendFormat:@" AND _registration = \"%@\" ", registrationUuid];
    }
    
    [query joinTo:[XapiAgentSrkObj class] leftParameter:@"_agent" targetParameter:@"Id"];
    if(agentMbox != nil) {
        [whereStr appendFormat:@" AND XapiAgentSrkObj._mbox = \"%@\" ", agentMbox];
    }
    
    [query where:whereStr];
    
    if(agentAccountName != nil && agentAccountHomepage != nil) {
        [whereStr appendFormat:@" AND XapiAgentSrkObj._accountName = \"%@\" AND XapiAgentSrkObj._accountHomepage = \"%@\" ", agentAccountName, agentAccountHomepage];
    }
    
    SRKResultSet *result = [[query where:whereStr]fetch];
    
    if([result count] > 0) {
        return [result objectAtIndex:0];
    }else {
        return nil;
    }
}

- (void)persistWithId:(id)dbContext
withComUstadmobileNanolrsCoreModelXapiState:(id<ComUstadmobileNanolrsCoreModelXapiState>)data {
    XapiStateSrkObj *obj = (XapiStateSrkObj *)data;
    [obj commit];
}

- (jboolean)delete__WithId:(id)dbContext withComUstadmobileNanolrsCoreModelXapiState:(id<ComUstadmobileNanolrsCoreModelXapiState>)data {
    XapiStateSrkObj *obj = (XapiStateSrkObj *)data;
    return [obj remove];
}


@end
