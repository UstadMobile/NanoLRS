//
//  NanoLrsManagerSharkORM.h
//  NanoLrsLibiOS
//
//  Created by Mike Dawson on 09/02/2017.
//  Copyright © 2017 UstadMobile FZ-LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <SharkORM/SharkORM.h>
#import "java/util/List.h"
#import "java/util/Arrays.h"
#import "NanoLrsBaseManagerSharkORM.h"

@interface NanoLrsBaseManagerSharkORM : NSObject

- (id) returnFirstResultWithSRKResultSet:(SRKResultSet *)resultSet;

- (id<JavaUtilList>) makeJavaUtilListWithSRKResultSet:(SRKResultSet *)resultSet withClassType:(IOSClass *)classType;
@end
