package info.fmro.betty.main;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    info.fmro.betty.main.ScraperThreadTest.class,
    info.fmro.betty.main.VarsIOTest.class,
//    info.fmro.betty.main.CoralScraperThreadIT.class,
//    info.fmro.betty.main.BettyIT.class,
    info.fmro.betty.main.QuickCheckThreadTest.class
})
public class BettyMainTestSuite {

    @BeforeClass
    public static void setUpClass()
            throws NotFoundException, CannotCompileException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException { // runs before initialization of program statics
        final ClassPool classPool = ClassPool.getDefault();
        final CtClass factoryClass = classPool.getCtClass("info.fmro.betty.objects.Statics");
        final CtField existingField = factoryClass.getField("closeStandardStreamsNotInitialized");
        factoryClass.removeField(existingField);
        final CtField newField = CtField.make("public static final boolean closeStandardStreamsNotInitialized = true;", factoryClass);
        factoryClass.addField(newField);
        factoryClass.toClass();
    }

    @AfterClass
    public static void tearDownClass()
            throws Exception {
    }

    @Before
    public void setUp()
            throws Exception {
    }

    @After
    public void tearDown()
            throws Exception {
    }
}
