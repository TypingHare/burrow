package burrow.core.common;

import org.springframework.lang.Nullable;

public final class Values {
    public static final String NULL = "null";

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
}
