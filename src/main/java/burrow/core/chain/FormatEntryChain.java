package burrow.core.chain;

import burrow.chain.Context;
import burrow.chain.Hook;
import burrow.chain.IdentityChain;
import burrow.core.common.ColorUtility;
import burrow.core.common.Values;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Map;

public final class FormatEntryChain extends IdentityChain<Context> {
    public static final Hook<Integer> idHook = Hook.of(ContextKey.ID, Integer.class);
    public static final Hook<Map<String, String>> formattedObjectHook =
        Hook.of(ContextKey.FORMATTED_OBJECT);
    public static final Hook<String> resultHook = Hook.of(ContextKey.RESULT, String.class);

    public static void format(@NonNull final Context ctx) {
        final var id = idHook.get(ctx);
        final var formattedObject = formattedObjectHook.get(ctx);

        if (formattedObject == null) {
            resultHook.set(ctx, ColorUtility.render(Values.NULL, ColorUtility.Type.NULL));
        } else {
            final var lines = new ArrayList<String>();
            lines.add("[" + id + "] {");
            for (final var objectEntry : formattedObject.entrySet()) {
                final var key = ColorUtility.render(objectEntry.getKey(), ColorUtility.Type.KEY);
                final var value =
                    ColorUtility.render(objectEntry.getValue(), ColorUtility.Type.VALUE);
                lines.add("  " + key + ": " + value);
            }
            lines.add("}");

            resultHook.set(ctx, String.join("\n", lines));
        }
    }

    @NonNull
    public Context createContext(
        final int id,
        @Nullable final Map<String, String> entryObject
    ) {
        final var context = new Context();
        idHook.set(context, id);
        formattedObjectHook.set(context, entryObject);

        return context;
    }

    public static final class ContextKey {
        public static final String ID = "ID";
        public static final String FORMATTED_OBJECT = "FORMATTED_OBJECT";
        public static final String RESULT = "RESULT";
    }
}
