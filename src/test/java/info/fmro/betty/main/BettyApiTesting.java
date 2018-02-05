package info.fmro.betty.main;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.DebuggingMethods;
import info.fmro.shared.utility.Generic;
import java.lang.reflect.Field;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BettyApiTesting {

    private static final Logger logger = LoggerFactory.getLogger(BettyApiTesting.class);

    @Rule
    @SuppressWarnings("PublicField")
    public TestRule watchman = new TestWatcher() {
        @Override
        public void starting(Description description) { // rules wrap around the tests, including @Before and @After
            logger.info("{} being run...", description.getMethodName());
        }
    };

    public BettyApiTesting() {
    }

    @BeforeClass
    public static void setUpClass()
            throws NotFoundException, CannotCompileException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException { // runs before everything else
        // javassist version has slow .toClass() method
        final ClassPool classPool = ClassPool.getDefault();
        final CtClass factoryClass = classPool.getCtClass("info.fmro.betty.objects.Statics");
        final CtField existingField = factoryClass.getField("closeStandardStreamsNotInitialized");
        factoryClass.removeField(existingField);
        final CtField newField = CtField.make("public static final boolean closeStandardStreamsNotInitialized = true;", factoryClass);
        factoryClass.addField(newField);
        factoryClass.toClass();

        // lines necessary so that Statics.class is properly loaded and it's static fields initialised (or at least the method regarding standard streams is executed)
        Field field = Statics.class.getDeclaredField("closeStandardStreamsNotInitialized");
        field.get(null);

        // reflection version, although it modifies the value, doesn't change the behavior of the program (old value is still used for invoking the method during statics init)
//        Field field = Statics.class.getDeclaredField("closeStandardStreamsNotInitialized");
//        boolean newValue = true;
//        Generic.setFinalStatic(field, newValue);
        Generic.disableHTTPSValidation();
        Generic.turnOffHtmlUnitLogger();
        VarsIO.readVarsFromFile(Statics.VARS_FILE_NAME);
        VarsIO.readObjectsFromFiles();
        MaintenanceThread maintenanceThread = new MaintenanceThread();
        maintenanceThread.start();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void findNewMarketTypes() {
        DebuggingMethods.findNewMarketTypes();
        DebuggingMethods.listCurrentOrders();
        DebuggingMethods.listClearedtOrders();
//        DebuggingMethods.printMarketType("ACCA");
    }
}
