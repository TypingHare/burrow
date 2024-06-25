package burrow.core.chain;

import burrow.chain.Context;
import burrow.chain.IdentityChain;

public class FormatEntryChain extends IdentityChain<Context> {
    public static final class ContextKey {
        public static final String ENTRY = "ENTRY";
        public static final String LINES = "LINES";
    }
}
