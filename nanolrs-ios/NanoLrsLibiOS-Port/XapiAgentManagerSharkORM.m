//
//  XapiAgentManagerSharkORM.m
//  NanoLrsLibiOS
//
//  Created by Mike Dawson on 08/02/2017.
//  Copyright © 2017 UstadMobile FZ-LLC. All rights reserved.
//

#import "XapiAgentManagerSharkORM.h"
#import "XapiAgentSrkObj.h"
#import "XapiAgent.h"
#import "java/util/Arrays.h"
#import "java/util/ArrayList.h"

@implementation XapiAgentManagerSharkORM

- (id<JavaUtilList>)findAgentByParamsWithId:(id)dbContext
                               withNSString:(NSString *)mbox
                               withNSString:(NSString *)accountName
                               withNSString:(NSString *)accountHomepage {
    NSMutableString *whereStr = [[NSMutableString alloc]init];
    if(mbox != nil) {
        [whereStr appendFormat:@"_mbox = \"%@\"", mbox];
    }
    
    if(mbox != nil && accountName != nil) {
        [whereStr appendString:@" AND "];
    }
    
    if(accountName != nil) {
        [whereStr appendFormat:@"_accountName = \"%@\" AND _accountHomepage = \"%@\"", accountName, accountHomepage];
    }
    SRKResultSet *result = [[[XapiAgentSrkObj query]where:whereStr]fetch];
    IOSObjectArray *objArr = [IOSObjectArray arrayWithNSArray:result type:ComUstadmobileNanolrsCoreModelXapiAgent_class_()];
    id<JavaUtilList> list = [JavaUtilArrays asListWithNSObjectArray:objArr];
    
    return list;
}

- (id<ComUstadmobileNanolrsCoreModelXapiAgent>)makeNewWithId:(id)dbContext {
    return [XapiAgentSrkObj new];
}

- (void)createOrUpdateWithId:(id)dbContext
withComUstadmobileNanolrsCoreModelXapiAgent:(id<ComUstadmobileNanolrsCoreModelXapiAgent>)data {
    XapiAgentSrkObj *obj = (XapiAgentSrkObj *)data;
    [obj commit];
}



@end
