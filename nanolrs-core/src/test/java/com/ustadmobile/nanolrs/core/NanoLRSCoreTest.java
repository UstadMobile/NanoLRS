package com.ustadmobile.nanolrs.core;

import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base class for tests that allows for a core test to access implementation dependent context. Our
 * logic uses a system dependent context object : e.g. Context on Android, at least sometimes
 * AppDelegate on iOS. A test also tests the core functionality that is implementation independent.
 * Java does not support multiple inheritence.
 *
 *
 * Created by mike on 1/21/17.
 */

public abstract class NanoLRSCoreTest {

}
