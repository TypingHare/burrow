package burrow.core.common;

import org.springframework.lang.Nullable;

public final class Values {
    public static final String NULL = "null";
    public static final String EMPTY = "";

    public static final class Bool {
        public static final String TRUE = "true";
        public static final String FALSE = "false";

        public static boolean isTrue(@Nullable final String string) {
            return TRUE.equalsIgnoreCase(string);
        }

        public static boolean isFalse(@Nullable final String string) {
            return FALSE.equalsIgnoreCase(string);
        }

        public static String stringify(final boolean bool) {
            return bool ? TRUE : FALSE;
        }
    }

    public static final class Int {
        public static final String ZERO = "0";

        public static String stringify(final int value) {
            return String.valueOf(value);
        }
    }
}
