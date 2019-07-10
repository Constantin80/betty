package info.fmro.betty.objects;

import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Ignorable;
import info.fmro.shared.utility.SynchronizedMap;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

@SuppressWarnings({"ClassWithTooManyMethods", "UtilityClass"})
public final class BlackList {
    private static final Logger logger = LoggerFactory.getLogger(BlackList.class);
    public static final long defaultSafetyPeriod = 10_000L;

    @Contract(pure = true)
    private BlackList() {
    }

    @SuppressWarnings("unused")
    private static <T> int setIgnored(final Class<? extends Ignorable> clazz, final T key) {
        return setIgnored(clazz, key, BlackList.defaultSafetyPeriod);
    }

    private static <T> int setIgnored(final Class<? extends Ignorable> clazz, final T key, @SuppressWarnings("SameParameterValue") final long safetyPeriod) {
        final long currentTime = System.currentTimeMillis();
        return setIgnored(clazz, key, safetyPeriod, currentTime);
    }

    private static <T> int setIgnored(final Class<? extends Ignorable> clazz, final T key, final long safetyPeriod, final long currentTime) {
        @SuppressWarnings("unchecked") final SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);

        return setIgnored(synchronizedMap, key, safetyPeriod, currentTime);
    }

    @SuppressWarnings("unused")
    private static <T> int setIgnored(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final T key) {
        return setIgnored(synchronizedMap, key, BlackList.defaultSafetyPeriod);
    }

    private static <T> int setIgnored(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final T key, @SuppressWarnings("SameParameterValue") final long safetyPeriod) {
        final long currentTime = System.currentTimeMillis();
        return setIgnored(synchronizedMap, key, safetyPeriod, currentTime);
    }

    private static <T> int setIgnored(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final T key, final long safetyPeriod, final long currentTime) {
        final int modified;

        if (synchronizedMap == null) {
            logger.error("null synchronizedMap in setIgnored for {} {} {}", key, safetyPeriod, currentTime);
            modified = -10000;
        } else if (synchronizedMap.containsKey(key)) {
            final Ignorable value = synchronizedMap.get(key);
            if (value == null) {
                logger.error("null value in setIgnored for key: {} period: {}", key, safetyPeriod);
                modified = -10000; // there's an error
            } else {
                modified = value.setIgnored(safetyPeriod, currentTime);
            }
        } else {
            logger.error("attempted to setIgnored on non existing key: {} period: {}", key, safetyPeriod);
            modified = -10000; // not found
        }

        return modified;
    }

    public static <T> void printNotExistOrBannedErrorMessages(final Class<? extends Ignorable> clazz, final T key, final String format) {
        @SuppressWarnings("unchecked") final SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        final long currentTime = System.currentTimeMillis();

        printNotExistOrBannedErrorMessages(synchronizedMap, key, currentTime, Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD, format);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> void printNotExistOrBannedErrorMessages(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final T key, final String format) {
        final long currentTime = System.currentTimeMillis();
        printNotExistOrBannedErrorMessages(synchronizedMap, key, currentTime, Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD, format);
    }

    public static <T> void printNotExistOrBannedErrorMessages(final Class<? extends Ignorable> clazz, final T key, final long currentTime, final String format) {
        @SuppressWarnings("unchecked") final SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        printNotExistOrBannedErrorMessages(synchronizedMap, key, currentTime, Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD, format);
    }

    public static <T> void printNotExistOrBannedErrorMessages(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final T key, final long currentTime, final String format) {
        printNotExistOrBannedErrorMessages(synchronizedMap, key, currentTime, Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD, format);
    }

    public static <T> void printNotExistOrBannedErrorMessages(final Class<? extends Ignorable> clazz, final T key, final long currentTime, final long safetyPeriod, final String format) {
        @SuppressWarnings("unchecked") final SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        printNotExistOrBannedErrorMessages(synchronizedMap, key, currentTime, safetyPeriod, format);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> void printNotExistOrBannedErrorMessages(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final T key, final long currentTime, final long safetyPeriod, final String format) {
        if (notExist(synchronizedMap, key)) {
            final long timeSinceLastRemoved = timeSinceRemovalFromMap(synchronizedMap, currentTime);
            final String printedString =
                    MessageFormatter.arrayFormat("{} no value in map, timeSinceLastRemoved: {} for key: {}", new Object[]{format, timeSinceLastRemoved, key}).getMessage();
            if (timeSinceLastRemoved <= safetyPeriod) {
                logger.info(printedString);
            } else {
                logger.error(printedString);
            }
        } else {
            final long timeSinceBan = timeSinceBan(synchronizedMap, key, currentTime);
            final String printedString = MessageFormatter.arrayFormat("{} ignored for key: {} {}", new Object[]{format, timeSinceBan, key}).getMessage();
            if (timeSinceBan <= safetyPeriod) {
                logger.info(printedString);
            } else {
                logger.error(printedString);
            }
        }
    }

    public static <T> long timeSinceBan(final Ignorable ignorable, final T key) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceBan(ignorable, key, currentTime);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> long timeSinceBan(final Ignorable ignorable, final T key, final long currentTime) {
        final long result;
        if (ignorable == null) {
            logger.error("null ignorable in timeSinceBan for {} {}", key, currentTime);
            result = Long.MAX_VALUE;
        } else {
            final Class<? extends Ignorable> clazz = ignorable.getClass();
            result = timeSinceBan(clazz, key, currentTime);
        }

        return result;
    }

    public static <T> long timeSinceBan(final Class<? extends Ignorable> clazz, final T key) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceBan(clazz, key, currentTime);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> long timeSinceBan(final Class<? extends Ignorable> clazz, final T key, final long currentTime) {
        @SuppressWarnings("unchecked") final SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        return timeSinceBan(synchronizedMap, key, currentTime);
    }

    public static <T> long timeSinceBan(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final T key) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceBan(synchronizedMap, key, currentTime);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> long timeSinceBan(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final T key, final long currentTime) {
        final long result;
        if (synchronizedMap == null) {
            logger.error("null synchronizedMap in timeSinceBan for {} {}", key, currentTime);
            result = Long.MAX_VALUE;
        } else if (synchronizedMap.containsKey(key)) {
            final Ignorable value = synchronizedMap.get(key);
            if (value == null) {
                logger.error("null value in timeSinceBan for key {} {}", key, currentTime);
                result = Long.MAX_VALUE;
            } else {
                result = value.timeSinceSetIgnored(currentTime); // it does exist
            }
        } else {
            result = Long.MAX_VALUE;
        }

        return result;
    }

    public static long timeSinceRemovalFromMap(final Ignorable ignorable) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceRemovalFromMap(ignorable, currentTime);
    }

    @SuppressWarnings("WeakerAccess")
    public static long timeSinceRemovalFromMap(final Ignorable ignorable, final long currentTime) {
        final long result;
        if (ignorable == null) {
            logger.error("null ignorable in timeSinceRemovalFromMap: {}", currentTime);
            result = Long.MAX_VALUE;
        } else {
            final Class<? extends Ignorable> clazz = ignorable.getClass();
            result = timeSinceRemovalFromMap(clazz, currentTime);
        }

        return result;
    }

    public static long timeSinceRemovalFromMap(final Class<? extends Ignorable> clazz) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceRemovalFromMap(clazz, currentTime);
    }

    @SuppressWarnings("WeakerAccess")
    public static long timeSinceRemovalFromMap(final Class<? extends Ignorable> clazz, final long currentTime) {
        final SynchronizedMap<?, ? extends Ignorable> synchronizedMap = Formulas.getIgnorableMap(clazz);
        return timeSinceRemovalFromMap(synchronizedMap, currentTime);
    }

    public static <T> long timeSinceRemovalFromMap(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceRemovalFromMap(synchronizedMap, currentTime);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> long timeSinceRemovalFromMap(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final long currentTime) {
        final long result;
        if (synchronizedMap == null) {
            logger.error("null synchronizedMap in timeSinceRemovalFromMap: {}", currentTime);
            result = Long.MAX_VALUE;
        } else {
            final long timeStampRemoved = synchronizedMap.getTimeStampRemoved();
//            final long currentTime = System.currentTimeMillis();
            result = currentTime - timeStampRemoved;
        }

        return result;
    }

    public static <T> boolean notExist(final Ignorable ignorable, final T key) {
        final boolean result;
        if (ignorable == null) {
            logger.error("null ignorable in notExist for {}", key);
            result = true; // it's true that there's an error
        } else {
            final Class<? extends Ignorable> clazz = ignorable.getClass();
            result = notExist(clazz, key);
        }

        return result;
    }

    public static <T> boolean notExist(final Class<? extends Ignorable> clazz, final T key) {
        @SuppressWarnings("unchecked") final SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        return notExist(synchronizedMap, key);
    }

    public static <T> boolean notExist(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final T key) {
        final boolean result;

        if (synchronizedMap == null) {
            logger.error("null synchronizedMap in notExist for {}", key);
            result = true; // it's true that it doesn't exist
        } else if (synchronizedMap.containsKey(key)) {
            final Ignorable value = synchronizedMap.get(key);
            if (value == null) {
                logger.error("null value in notExist for key {}", key);
                result = true; // it's true that there's an error
            } else {
//                result = value.isIgnored(currentTime);
                result = false; // it does exist
            }
        } else {
            result = true; // it's true that it doesn't exist
        }

        return result;
    }

    public static <T> boolean notExistOrIgnored(final Ignorable ignorable, final T key) {
        final long currentTime = System.currentTimeMillis();
        return notExistOrIgnored(ignorable, key, currentTime);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> boolean notExistOrIgnored(final Ignorable ignorable, final T key, final long currentTime) {
        final boolean result;
        if (ignorable == null) {
            logger.error("null ignorable in notExistOrIgnored for {} {}", key, currentTime);
            result = true; // it's true that there's an error
        } else {
            final Class<? extends Ignorable> clazz = ignorable.getClass();
            result = notExistOrIgnored(clazz, key, currentTime);
        }

        return result;
    }

    public static <T> boolean notExistOrIgnored(final Class<? extends Ignorable> clazz, final T key) {
        final long currentTime = System.currentTimeMillis();
        return notExistOrIgnored(clazz, key, currentTime);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> boolean notExistOrIgnored(final Class<? extends Ignorable> clazz, final T key, final long currentTime) {
        @SuppressWarnings("unchecked") final SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        return notExistOrIgnored(synchronizedMap, key, currentTime);
    }

    @SuppressWarnings("WeakerAccess")
    public static <T> boolean notExistOrIgnored(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final T key) {
        final long currentTime = System.currentTimeMillis();
        return notExistOrIgnored(synchronizedMap, key, currentTime);
    }

    public static <T> boolean notExistOrIgnored(final SynchronizedMap<T, ? extends Ignorable> synchronizedMap, final T key, final long currentTime) {
        final boolean result;

        if (synchronizedMap == null) {
            logger.error("null synchronizedMap in notExistOrIgnored for {} {}", key, currentTime);
            result = true; // it's true that it doesn't exist
        } else if (synchronizedMap.containsKey(key)) {
            final Ignorable value = synchronizedMap.get(key);
            if (value == null) {
                logger.error("null value in notExistOrIgnored for key {}", key);
                result = true; // it's true that there's an error
            } else {
                result = value.isIgnored(currentTime);
            }
        } else {
            result = true; // it's true that it doesn't exist
        }

        return result;
    }
}
