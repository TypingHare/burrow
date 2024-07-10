package burrow.chain;

import burrow.chain.event.Event;
import burrow.chain.event.ThrowableEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChainTest {
    private static final class SimpleChain extends Chain<Context> {
        @NotNull
        public Context apply(@NotNull final String message) {
            final var context = new Context();
            Hook.message.set(context, message);

            return apply(context);
        }

        @NotNull
        public Context apply(final int integer) {
            final var context = new Context();
            Hook.integer.set(context, integer);

            return apply(context);
        }

        public @interface Hook {
            ContextHook<String> message = Context.hook("message");
            ContextHook<Integer> integer = Context.hook("integer");
        }
    }

    private static final class SimpleEvent extends Event {
        private final String word;

        private SimpleEvent(final String word) {
            this.word = word;
        }

        public String getWord() {
            return word;
        }

        public static void triggerWhenAuthorDetected(
            @NotNull final Context context,
            @Nullable final Runnable next
        ) {
            final var message = SimpleChain.Hook.message.getNonNull(context);
            if (message.startsWith("author:")) {
                context.trigger(new SimpleEvent("James"));
            }

            Chain.runIfNotNull(next);
        }

        public static void changeAuthorName(
            @NotNull final Context context,
            @NotNull final SimpleEvent event
        ) {
            SimpleChain.Hook.message.set(context, "author: " + event.getWord());
        }
    }

    @Test
    public void testApplyWithoutMiddlewares() {
        final var simpleChain = new SimpleChain();

        final var context = simpleChain.apply("Hello world!");
        Assertions.assertEquals("Hello world!", SimpleChain.Hook.message.get(context));
    }

    @Test
    public void testApplyWithMiddleware() {
        final var simpleChain = new SimpleChain();
        simpleChain.use((context, next) -> SimpleChain.Hook.message.compute(context, v -> v + "!"));

        final var context = simpleChain.apply("Hello world");
        Assertions.assertEquals("Hello world!", SimpleChain.Hook.message.get(context));
    }

    @Test
    public void testApplyWithMultipleMiddlewares() {
        final var simpleChain = new SimpleChain();
        simpleChain.use((context, next) -> {
            SimpleChain.Hook.integer.compute(context, v -> v + 2);
            if (next != null) next.run();
        });
        simpleChain.use((context, next) -> {
            SimpleChain.Hook.integer.compute(context, v -> v * 3);
            if (next != null) next.run();
        });

        final var context = simpleChain.apply(2);
        Assertions.assertEquals(12, SimpleChain.Hook.integer.get(context));
    }

    @Test
    public void testApplyWithPreProcessors() {
        final Middleware.Pre<Context> plus2 = (context) -> {
            SimpleChain.Hook.integer.compute(context, v -> v + 2);
        };
        final Middleware.Pre<Context> times3 = (context) -> {
            SimpleChain.Hook.integer.compute(context, v -> v * 3);
        };

        final var simpleChain = new SimpleChain();
        simpleChain.use(plus2);
        simpleChain.use(times3);

        final var context = simpleChain.apply(2);
        Assertions.assertEquals(12, SimpleChain.Hook.integer.get(context));
    }

    @Test
    public void testErrorHandle() {
        final var simpleChain = new SimpleChain();
        simpleChain.use((Middleware.Pre<Context>) (context) -> {
            final var message = SimpleChain.Hook.message.get(context);
            if (message != null) {
                SimpleChain.Hook.integer.set(context, Integer.parseInt(message));
            }
        });

        simpleChain.on(ThrowableEvent.class, (context, event) -> {
            final var message = SimpleChain.Hook.message.get(context);
            if (message != null) {
                final var secondChar = String.valueOf(message.charAt(1));
                SimpleChain.Hook.integer.set(context, Integer.parseInt(secondChar));
            }
        });

        final var context = simpleChain.apply("f2f");
        Assertions.assertEquals(2, SimpleChain.Hook.integer.get(context));
    }

    @Test
    public void testMiddlewarePost() {
        final var integerHook = SimpleChain.Hook.integer;
        final Middleware.Post<Context> plus2 = (context) -> {
            integerHook.compute(context, v -> v + 2);
        };
        final Middleware.Pre<Context> times3 = (context) -> {
            integerHook.compute(context, v -> v * 3);
        };

        final var simpleChain = new SimpleChain();
        simpleChain.use(plus2);
        simpleChain.use(times3);

        final var context = simpleChain.apply(2);
        Assertions.assertEquals(8, integerHook.get(context));
    }

    @Test
    public void testTriggerEvent() {
        final var simpleChain = new SimpleChain();
        final var messageHook = SimpleChain.Hook.message;
        simpleChain.use((Middleware.Pre<Context>) (context) -> {
            if (messageHook.getNonNull(context).startsWith("author:")) {
                context.trigger(new SimpleEvent("James"));
            }
        });

        simpleChain.on(SimpleEvent.class, (context, event) -> {
            messageHook.set(context, "author: " + event.getWord());
        });

        final var context1 = simpleChain.apply("age: 25");
        Assertions.assertEquals("age: 25", messageHook.get(context1));

        final var context2 = simpleChain.apply("author: Andrew");
        Assertions.assertEquals("author: James", messageHook.get(context2));
    }

    @Test
    public void testSetEventHandlerUsingFunction() {
        final var simpleChain = new SimpleChain();
        simpleChain.use(SimpleEvent::triggerWhenAuthorDetected);
        simpleChain.on(SimpleEvent.class, SimpleEvent::changeAuthorName);

        final var context = simpleChain.apply("author: Andrew");
        Assertions.assertEquals("author: James", SimpleChain.Hook.message.get(context));
    }

    @Test
    public void testUseFirst() {
        final var hook = SimpleChain.Hook.integer;
        final Middleware.Pre<Context> plus2 = (ctx) -> hook.compute(ctx, val -> val + 2);
        final Middleware.Pre<Context> times3 = (ctx) -> hook.compute(ctx, val -> val * 3);

        final var simpleChain = new SimpleChain();
        simpleChain.use(plus2);
        simpleChain.useFirst(times3);

        final var context = simpleChain.apply(5);
        Assertions.assertEquals(17, hook.get(context));
    }
}
