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
    [whereStr appendFormat:@"_activity = \"%@\" AND _stateId = \"%@\" ", activityId, stateId];
    if(stateId != nil) {
        [whereStr appendFormat:@" AND _registration = \"%@\" ", registrationUuid];
    }
    
    if(agentMbox != nil) {
        [whereStr appendFormat:@" AND _agent._mbox = \"%@\" ", agentMbox];
    }
    
    if(agentAccountName != nil && agentAccountHomepage != nil) {
        [whereStr appendFormat:@" AND _agent._accountName = \"%@\" AND _agent._accountHomepage = \"%@\" ", agentAccountName, agentAccountHomepage];
    }
    
    SRKResultSet *result = [[[XapiStateSrkObj query]where:whereStr]fetch];
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


@end
