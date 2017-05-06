package com.ustadmobile.nanolrs.test.core;


/**
 * Base class for tests that allows for a core test to access implementation dependent context. Our
 * logic uses a system dependent context object : e.g. Context on Android, at least sometimes
 * AppDelegate on iOS. A test also tests the core functionality that is implementation independent.
 * Java does not support multiple inheritence.
 *
 *
 * Created by mike on 1/21/17.
 */

public abstract class NanoLrsCoreTest {

}
