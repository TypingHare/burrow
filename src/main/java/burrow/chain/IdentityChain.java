package burrow.chain;

import org.springframework.lang.NonNull;

public class IdentityChain<C extends Context> extends Chain<C, C> {
    @Override
    @NonNull
    public final C createContext(@NonNull final C request) {
        return request;
    }
}
