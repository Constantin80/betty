package info.fmro.betty.main;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.permanent.MaintenanceThread;
import info.fmro.shared.utility.Generic;
import org.junit.jupiter.api.BeforeAll;

class BettyApiTesting
        extends ApiDefault {
    @SuppressWarnings("ThrowsRuntimeException")
    @BeforeAll
    public static void setUpClass()
            throws IllegalArgumentException {
        // reflection version, although it modifies the value, doesn't change the behavior of the program (old value is still used for invoking the method during statics init)
//        Field field = Statics.class.getDeclaredField("closeStandardStreamsNotInitialized");
//        boolean newValue = true;
//        Generic.setFinalStatic(field, newValue);
        Generic.disableHTTPSValidation();
        Generic.turnOffHtmlUnitLogger();
        VarsIO.readVarsFromFile(Statics.VARS_FILE_NAME);
        VarsIO.readObjectsFromFiles();
        final MaintenanceThread maintenanceThread = new MaintenanceThread();
        maintenanceThread.start();
    }

    // I can't run these integration tests now, and I'm not convinced on how good this test is, or what it does; disabled for now
//    @Test
//    void findNewMarketTypes() {
//        DebuggingMethods.findNewMarketTypes();
//        DebuggingMethods.listCurrentOrders();
//        DebuggingMethods.listClearedOrders();
////        DebuggingMethods.printMarketType("ACCA");
//    }
}
