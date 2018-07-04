package info.fmro.betty.main;

import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.fail;

public class ApiDefault {
    private final static AtomicInteger hasRun = new AtomicInteger(), initializationFinished = new AtomicInteger(), strangeFailureHappened = new AtomicInteger();
    private static final int MAX_WHILE_COUNTER = 30;

    @BeforeAll
    public synchronized static void defaultSetUpClass()
            throws InterruptedException { // runs before everything else
        // javassist version has slow .toClass() method
        if (hasRun.getAndIncrement() == 0) {
//            final ClassPool classPool = ClassPool.getDefault();
//            final CtClass factoryClass = classPool.getCtClass("info.fmro.betty.objects.Statics");
//            final CtField existingField = factoryClass.getField("closeStandardStreamsNotInitialized");
//            factoryClass.removeField(existingField);
////            final CtField newField = CtField.make("public static final boolean closeStandardStreamsNotInitialized = true;", factoryClass);
//            final CtField newField = CtField.make("private static boolean closeStandardStreamsNotInitialized = true;", factoryClass);
//            factoryClass.addField(newField);
////            factoryClass.toClass();

            // lines necessary so that Statics.class is properly loaded and it's static fields initialised (or at least the method regarding standard streams is executed)
//            final Field field = Statics.class.getDeclaredField("closeStandardStreamsNotInitialized");
//            field.setAccessible(true);
//            field.get(null);

            initializationFinished.getAndIncrement();
        } else if (initializationFinished.get() == 0) { // has run, but not yet initialized
            int whileCounter = 0;
            while (strangeFailureHappened.get() == 0 && initializationFinished.get() == 0 && whileCounter < MAX_WHILE_COUNTER) {
                Thread.sleep(100L);
                whileCounter++;
            }
            if (whileCounter >= MAX_WHILE_COUNTER) {
                strangeFailureHappened.getAndIncrement();
                fail("strange initialization failure in ApiDefault: " + hasRun.get() + " " + initializationFinished.get() + " " + whileCounter);
            }
        } else { // has run and is initialized, nothing to be done
        }
    }
}
