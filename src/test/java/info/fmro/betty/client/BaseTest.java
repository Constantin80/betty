package info.fmro.betty.client;

import info.fmro.betty.main.MaintenanceThread;
import info.fmro.betty.main.VarsIO;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.BeforeAll;

@SuppressWarnings("UtilityClass")
public final class BaseTest {
    @Contract(pure = true)
    private BaseTest() {
    }

    @BeforeAll
    public static void beforeClass() {
        Generic.disableHTTPSValidation();
        Generic.turnOffHtmlUnitLogger();
        VarsIO.readVarsFromFile(Statics.VARS_FILE_NAME);
        VarsIO.readObjectsFromFiles();
        final MaintenanceThread maintenanceThread = new MaintenanceThread();
        maintenanceThread.start();
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
