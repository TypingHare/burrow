package burrow.chain;

import burrow.chain.event.Event;
import burrow.chain.event.ThrowableEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;

public class ChainTest {
    private static final class SimpleChain extends Chain<Context, String> {
        public static final String CTX_REQUEST = "request";

        @NonNull
        @Override
        public Context createContext(@NonNull final String request) {
            final var context = new Context();
            context.set(CTX_REQUEST, request);

            return context;
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

        public static void triggerWhenAuthorDetected(@NonNull final Context ctx) {
            final var request = ctx.get(SimpleChain.CTX_REQUEST, String.class);
            if (request != null && request.startsWith("author:")) {
                ctx.trigger(new SimpleEvent("James"));
            }
        }

        public static void changeAuthorName(
            @NonNull final Context ctx,
            @NonNull final SimpleEvent event
        ) {
            ctx.set(SimpleChain.CTX_REQUEST, "author: " + event.getWord());
        }
    }

    @Test
    public void testApplyWithoutMiddlewares() {
        final var simpleChain = new SimpleChain();
        final var requestString = "Request String";
        final var context = simpleChain.apply(requestString);

        Assertions.assertEquals(requestString, context.get(SimpleChain.CTX_REQUEST));
    }

    @Test
    public void testApplyWithMiddleware() {
        final var simpleChain = new SimpleChain();
        simpleChain.use((ctx, next) -> ctx.compute(SimpleChain.CTX_REQUEST, (val) -> val + " 2"));

        final var requestString = "Request String";
        final var context = simpleChain.apply(requestString);

        final var expectedString = requestString + " 2";
        Assertions.assertEquals(expectedString, context.get(SimpleChain.CTX_REQUEST));
    }

    @Test
    public void testApplyWithMultipleMiddlewares() {
        final var simpleChain = new SimpleChain();
        simpleChain.use(((ctx, next) -> {
            ctx.compute(SimpleChain.CTX_REQUEST, (val) -> Integer.parseInt((String) val));
            next.run();
        }));
        simpleChain.use(((ctx, next) -> {
            ctx.compute(SimpleChain.CTX_REQUEST, Integer.class, (val) -> val + 2);
            next.run();
        }));
        simpleChain.use((ctx, next) -> {
            ctx.compute(SimpleChain.CTX_REQUEST, Integer.class, (val) -> val * 3);
            next.run();
        });

        final var context = simpleChain.apply("1");
        Assertions.assertEquals(9, context.get(SimpleChain.CTX_REQUEST, Integer.class));
    }

    @Test
    public void testApplyWithPreProcessors() {
        final Middleware.Pre<Context> toInteger = (ctx) ->
            ctx.compute(SimpleChain.CTX_REQUEST, (val) -> Integer.parseInt((String) val));
        final Middleware.Pre<Context> plus2 = (ctx) ->
            ctx.compute(SimpleChain.CTX_REQUEST, Integer.class, (val) -> val + 2);
        final Middleware.Pre<Context> times3 = (ctx) ->
            ctx.compute(SimpleChain.CTX_REQUEST, Integer.class, (val) -> val * 3);

        final var simpleChain = new SimpleChain();
        simpleChain.pre.use(toInteger);
        simpleChain.pre.use(plus2);
        simpleChain.pre.use(times3);

        final var context = simpleChain.apply("2");
        Assertions.assertEquals(12, context.get(SimpleChain.CTX_REQUEST, Integer.class));
    }

    @Test
    public void testHook() {
        final Middleware.Pre<Context> toInteger = (ctx) ->
            ctx.compute(SimpleChain.CTX_REQUEST, val -> Integer.parseInt((String) val));
        final var hook = Hook.of(SimpleChain.CTX_REQUEST, Integer.class);
        final Middleware.Pre<Context> plus2 = (ctx) -> hook.compute(ctx, val -> val + 2);
        final Middleware.Pre<Context> times3 = (ctx) -> hook.compute(ctx, val -> val * 3);

        final var simpleChain = new SimpleChain();
        simpleChain.pre.use(toInteger);
        simpleChain.pre.use(plus2);
        simpleChain.pre.use(times3);

        final var context = simpleChain.apply("2");
        Assertions.assertEquals(12, hook.get(context));
    }

    @Test
    public void testErrorHandle() {
        final var simpleChain = new SimpleChain();
        simpleChain.pre.use((ctx)
            -> ctx.compute(SimpleChain.CTX_REQUEST, val -> Integer.parseInt((String) val)));
        simpleChain.on(ThrowableEvent.class, (ctx, event) -> {
            ctx.compute(SimpleChain.CTX_REQUEST, val -> ((String) val).charAt(1));
        });

        final var context = simpleChain.apply("f2f");
        Assertions.assertEquals('2', context.get(SimpleChain.CTX_REQUEST));
    }

    @Test
    public void testMiddlewarePost() {
        final Middleware.Pre<Context> toInteger = (ctx) ->
            ctx.compute(SimpleChain.CTX_REQUEST, val -> Integer.parseInt((String) val));
        final var hook = Hook.of(SimpleChain.CTX_REQUEST, Integer.class);
        final Middleware.Post<Context> plus2 = (ctx) -> hook.compute(ctx, val -> val + 2);
        final Middleware.Pre<Context> times3 = (ctx) -> hook.compute(ctx, val -> val * 3);

        final var simpleChain = new SimpleChain();
        simpleChain.pre.use(toInteger);
        simpleChain.post.use(plus2);
        simpleChain.pre.use(times3);

        final var context = simpleChain.apply("2");
        Assertions.assertEquals(8, hook.get(context));
    }

    @Test
    public void testTriggerEvent() {
        final var simpleChain = new SimpleChain();
        final var hook = Hook.of(SimpleChain.CTX_REQUEST, String.class);
        simpleChain.pre.use((ctx) -> {
            if (hook.get(ctx).startsWith("author:")) {
                ctx.trigger(new SimpleEvent("James"));
            }
        });

        simpleChain.on(SimpleEvent.class, (ctx, event) -> {
            hook.set(ctx, "author: " + event.getWord());
        });

        final var context1 = simpleChain.apply("age: 25");
        Assertions.assertEquals("age: 25", hook.get(context1));

        final var context2 = simpleChain.apply("author: Andrew");
        Assertions.assertEquals("author: James", hook.get(context2));
    }

    @Test
    public void testSetEventHandlerUsingFunction() {
        final var simpleChain = new SimpleChain();
        final var hook = Hook.of(SimpleChain.CTX_REQUEST, String.class);
        simpleChain.pre.use(SimpleEvent::triggerWhenAuthorDetected);
        simpleChain.on(SimpleEvent.class, SimpleEvent::changeAuthorName);

        final var context = simpleChain.apply("author: Andrew");
        Assertions.assertEquals("author: James", hook.get(context));
    }

    @Test
    public void testUseFirst() {

    }
}
