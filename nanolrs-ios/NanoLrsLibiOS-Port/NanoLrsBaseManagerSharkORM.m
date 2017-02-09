//
//  NanoLrsManagerSharkORM.m
//  NanoLrsLibiOS
//
//  Created by Mike Dawson on 09/02/2017.
//  Copyright © 2017 UstadMobile FZ-LLC. All rights reserved.
//

#import "NanoLrsBaseManagerSharkORM.h"
#import "java/util/List.h"
#import "java/util/Arrays.h"

@implementation NanoLrsBaseManagerSharkORM

- (id) returnFirstResultWithSRKResultSet:(SRKResultSet *)resultSet {
    if([resultSet count] > 0) {
        return [resultSet objectAtIndex:0];
    }else {
        return nil;
    }
}

- (id<JavaUtilList>) makeJavaUtilListWithSRKResultSet:(SRKResultSet *)resultSet withClassType:(IOSClass *)classType {
    IOSObjectArray *objArr = [IOSObjectArray arrayWithNSArray:resultSet type:classType];
    return [JavaUtilArrays asListWithNSObjectArray:objArr];
}

@end
