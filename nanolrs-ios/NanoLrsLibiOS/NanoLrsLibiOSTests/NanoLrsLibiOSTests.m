//
//  NanoLrsLibiOSTests.m
//  NanoLrsLibiOSTests
//
//  Created by Mike Dawson on 30/01/2017.
//  Copyright © 2017 UstadMobile FZ-LLC. All rights reserved.
//
#include "J2ObjC_source.h"
#import <XCTest/XCTest.h>
#import "org/junit/runner/JUnitCore.h"
#import "org/junit/runner/Result.h"
#import "org/junit/runner/notification/Failure.h"
#import "java/util/List.h"
#import "TestParseUtils.h"
#import "TestJsonUtil.h"
#import "TestXapiActivityEndpointCore.h"
#import "TestXapiAgentEndpointCore.h"
#import "TestXapiStateEndpointCore.h"
#import "TestXapiHttpdState.h"
#import "PersistenceManager.h"
#import "TestXapiStatement.h"
#import "TestXapiHttpdStatements.h"
#import "TestXapiForwardingStatement.h"

@interface NanoLrsLibiOSTests : XCTestCase

@end

@implementation NanoLrsLibiOSTests

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)listFailuresWithResult:(OrgJunitRunnerResult *)result {
    id<JavaUtilList> failures = [result getFailures];
    for(int i = 0; i < [failures size]; i++) {
        OrgJunitRunnerNotificationFailure *failure = [failures getWithInt:i];
        NSLog(@"%@: %@", [failure getTestHeader], [failure getMessage]);
        NSLog(@"%@", [failure getTrace]);
    }
}

-(OrgJunitRunnerResult *)runJunitWithIOSClass:(IOSClass *)cls {
    OrgJunitRunnerJUnitCore *junitCore = [[OrgJunitRunnerJUnitCore alloc]init];
    OrgJunitRunnerResult *result = [junitCore runWithIOSClassArray:[IOSObjectArray arrayWithNSArray:@[cls] type:IOSClass_class_()]];
    [self listFailuresWithResult:result];
    return result;
}

- (void)testExample {
    // This is an example of a functional test case.
    // Use XCTAssert and related functions to verify your tests produce the correct results.
}

- (void)testRunningJunit {
    NSArray *testNames = @[@"com.ustadmobile.nanolrs.core.util.TestJsonUtil"];
    OrgJunitRunnerJUnitCore *junitCore = [[OrgJunitRunnerJUnitCore alloc]init];
    IOSClass *cls = [IOSClass forName:@"com.ustadmobile.nanolrs.core.util.TestJsonUtil"];
    OrgJunitRunnerResult *result = [junitCore runWithIOSClassArray:[IOSObjectArray arrayWithNSArray:@[cls] type:[cls getClass]]];
    XCTAssert([result getFailureCount] == 0);
}

-(void)testActivityEndpoint {
    OrgJunitRunnerJUnitCore *junitCore = [[OrgJunitRunnerJUnitCore alloc]init];
    OrgJunitRunnerResult *result = [junitCore runWithIOSClassArray:[IOSObjectArray arrayWithNSArray:@[ComUstadmobileNanolrsCoreEndpointsTestXapiActivityEndpointCore_class_()] type:IOSClass_class_()]];
    [self listFailuresWithResult:result];
    XCTAssert([result getFailureCount] == 0);
}

-(void)testAgentEndpoint {
    OrgJunitRunnerJUnitCore *junitCore = [[OrgJunitRunnerJUnitCore alloc]init];
    OrgJunitRunnerResult *result = [junitCore runWithIOSClassArray:[IOSObjectArray arrayWithNSArray:@[ComUstadmobileNanolrsCoreEndpointsTestXapiAgentEndpointCore_class_()] type:IOSClass_class_()]];
    [self listFailuresWithResult:result];
    XCTAssert([result getFailureCount] == 0);
}

-(void)testStateEndpoint {
    OrgJunitRunnerJUnitCore *junitCore = [[OrgJunitRunnerJUnitCore alloc]init];
    OrgJunitRunnerResult *result = [junitCore runWithIOSClassArray:[IOSObjectArray arrayWithNSArray:@[ComUstadmobileNanolrsCoreEndpointsTestXapiStateEndpointCore_class_()] type:IOSClass_class_()]];
    [self listFailuresWithResult:result];
    XCTAssert([result getFailureCount] == 0);
}

-(void)testStateHttp {
    OrgJunitRunnerJUnitCore *junitCore = [[OrgJunitRunnerJUnitCore alloc]init];
    OrgJunitRunnerResult *result = [junitCore runWithIOSClassArray:[IOSObjectArray arrayWithNSArray:@[ComUstadmobileNanolrsHttpdTestXapiHttpdState_class_()] type:IOSClass_class_()]];
    [self listFailuresWithResult:result];
    XCTAssert([result getFailureCount] == 0);
}

-(void)testStatement {
    OrgJunitRunnerJUnitCore *junitCore = [[OrgJunitRunnerJUnitCore alloc]init];
    OrgJunitRunnerResult *result = [junitCore runWithIOSClassArray:[IOSObjectArray arrayWithNSArray:@[ComUstadmobileNanolrsCoreModelTestXapiStatement_class_()] type:IOSClass_class_()]];
    [self listFailuresWithResult:result];
    XCTAssert([result getFailureCount] == 0);
}

-(void)testHttpdStatement {
    OrgJunitRunnerResult *result = [self runJunitWithIOSClass:ComUstadmobileNanolrsHttpdTestXapiHttpdStatements_class_()];
    XCTAssert([result getFailureCount] == 0);
}

-(void)testForwardingStatement {
    OrgJunitRunnerResult *result = [self runJunitWithIOSClass:ComUstadmobileNanolrsCoreModelTestXapiForwardingStatement_class_()];
    XCTAssert([result getFailureCount] == 0);
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
