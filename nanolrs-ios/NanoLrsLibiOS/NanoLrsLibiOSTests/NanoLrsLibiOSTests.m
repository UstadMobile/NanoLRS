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
#import "TestParseUtils.h"
#import "TestJsonUtil.h"

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

- (void)testExample {
    // This is an example of a functional test case.
    // Use XCTAssert and related functions to verify your tests produce the correct results.
}

- (void)testRunningJunit {
    //NSArray *testNames = @[@"com.ustadmobile.nanolrs.core.util.TestParseUtils"];
    NSArray *testNames = @[@"com.ustadmobile.nanolrs.core.util.TestJsonUtil"];
    IOSObjectArray *testArgs = [IOSObjectArray arrayWithNSArray:testNames type:NSString_class_()];
    [OrgJunitRunnerJUnitCore mainWithNSStringArray:testArgs];
    XCTAssert(@"Ran Junit");
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
