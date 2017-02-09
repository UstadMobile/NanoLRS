//
//  XapiVerbManagerSharkORM.m
//  NanoLrsLibiOS
//
//  Created by Mike Dawson on 09/02/2017.
//  Copyright © 2017 UstadMobile FZ-LLC. All rights reserved.
//

#import "XapiVerbManagerSharkORM.h"


@implementation XapiVerbManagerSharkORM

- (id<ComUstadmobileNanolrsCoreModelXapiVerb>)makeWithId:(id)dbContext
                                            withNSString:(NSString *)verbId {
    return [[XapiVerbSrkObj alloc]initWithPrimaryKeyValue:verbId];
}

- (void)persistWithId:(id)dbContext
withComUstadmobileNanolrsCoreModelXapiVerb:(id<ComUstadmobileNanolrsCoreModelXapiVerb>)data {
    XapiVerbSrkObj *dataSrkObj = (XapiVerbSrkObj *)data;
    [dataSrkObj commit];
}

- (id<ComUstadmobileNanolrsCoreModelXapiVerb>)findByIdWithId:(id)dbContext
                                                withNSString:(NSString *)id_ {
    SRKResultSet *result = [[[XapiVerbSrkObj query]whereWithFormat:@"Id = %@" withParameters:@[id_]]fetch];
    return [self returnFirstResultWithSRKResultSet:result];
}


@end
