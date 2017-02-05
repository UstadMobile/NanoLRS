//
//  ActivityManagerSharkORM.m
//  NanoLrsLibiOS
//
//  Created by Mike Dawson on 05/02/2017.
//  Copyright © 2017 UstadMobile FZ-LLC. All rights reserved.
//

#import "XapiActivityManagerSharkORM.h"
#import "XapiActivitySrkObj.h"

@implementation XapiActivityManagerSharkORM

- (id<ComUstadmobileNanolrsCoreModelXapiActivity>)findByActivityIdWithId:(id)dbContext
                                                            withNSString:(NSString *)id_ {
    NSString *whereStr = [NSString stringWithFormat:@"%@%@%@", @"Id = \"", id_, @"\""];
    SRKResultSet *result = [[[XapiActivitySrkObj query]where:whereStr]fetch];
    if([result count] > 0) {
        return [result objectAtIndex:0];
    }else {
        return nil;
    }
}

- (id<ComUstadmobileNanolrsCoreModelXapiActivity>)makeNewWithId:(id)dbContext {
    return [XapiActivitySrkObj new];
}

- (void)createOrUpdateWithId:(id)dbContext
withComUstadmobileNanolrsCoreModelXapiActivity:(id<ComUstadmobileNanolrsCoreModelXapiActivity>)data {
    [(XapiActivitySrkObj *)data commit];
}

- (void)deleteByActivityIdWithId:(id)dbContext
                    withNSString:(NSString *)id_ {
    [[[[XapiActivitySrkObj query]where:[NSString stringWithFormat:@"%@%@%@", @"Id = \"", id_, @"\""]]fetch] removeAll];
}


@end
