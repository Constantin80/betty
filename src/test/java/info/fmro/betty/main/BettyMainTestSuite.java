package info.fmro.betty.main;

import org.junit.jupiter.api.AfterAll;

// Junit 5 doesn't have test suite support now; I can use the Junit 4 test suite if needed, or wait for Junit 5 support
//@ExtendWith(JUnitPlatform.class)
//@Suite.SuiteClasses({
//    info.fmro.betty.main.ScraperPermanentThreadTest.class,
//    info.fmro.betty.main.VarsIOTest.class,
////    info.fmro.betty.main.CoralScraperThreadIT.class,
////    info.fmro.betty.main.BettyIT.class,
//    info.fmro.betty.main.QuickCheckThreadTest.class
//})
class BettyMainTestSuite
        extends ApiDefault {
    // no longer needed after extending ApiDefault
//    @BeforeAll
//    public static void setUpClass()
//            throws NotFoundException, CannotCompileException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException { // runs before initialization of program statics
//        final ClassPool classPool = ClassPool.getDefault();
//        final CtClass factoryClass = classPool.getCtClass("info.fmro.betty.objects.Statics");
//        final CtField existingField = factoryClass.getField("closeStandardStreamsNotInitialized");
//        factoryClass.removeField(existingField);
//        final CtField newField = CtField.make("public static final boolean closeStandardStreamsNotInitialized = true;", factoryClass);
//        factoryClass.addField(newField);
//        factoryClass.toClass();
//    }
    @AfterAll
    public static void tearDownClass() {
    }
}
