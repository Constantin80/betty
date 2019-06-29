package info.fmro.betty.client;

import info.fmro.betty.main.MaintenanceThread;
import info.fmro.betty.main.VarsIO;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.junit.jupiter.api.BeforeAll;

/**
 * Created by mulveyj on 08/07/2016.
 */
public class BaseTest {
    private static String appKey;
    private static String userName;
    private static String password;

    @BeforeAll
    public static void beforeClass() {
        Generic.disableHTTPSValidation();
        Generic.turnOffHtmlUnitLogger();
        VarsIO.readVarsFromFile(Statics.VARS_FILE_NAME);
        VarsIO.readObjectsFromFiles();
        MaintenanceThread maintenanceThread = new MaintenanceThread();
        maintenanceThread.start();

        appKey = Statics.appKey.get();
        userName = Statics.bu.get();
        password = Statics.bp.get();
    }

//    private static String getSystemProperty(String key) {
//        String value = System.getProperty(key);
//        if (value == null) {
//            throw new IllegalArgumentException(String.format("System property %s must be set for tests to run", key));
//        }
//        return value;
//    }

    public static String getAppKey() {
        return appKey;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getPassword() {
        return password;
    }

//    public AppKeyAndSessionProvider getValidSessionProvider() {
//        return new AppKeyAndSessionProvider(
//                Statics.SSO_HOST_RO,
//                appKey,
//                userName,
//                password);
//    }
//
//    public AppKeyAndSessionProvider getInvalidHostSessionProvider() {
//        return new AppKeyAndSessionProvider(
//                "www.betfair.com",
//                "a",
//                "b",
//                "c");
//    }
//
//    public AppKeyAndSessionProvider getInvalidLoginSessionProvider() {
//        return new AppKeyAndSessionProvider(
//                Statics.SSO_HOST_RO,
//                "a",
//                "b",
//                "c");
//    }
}
