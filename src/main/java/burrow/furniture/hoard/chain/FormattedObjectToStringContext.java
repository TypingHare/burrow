package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.core.common.Environment;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

public class FormattedObjectToStringContext extends Context {
    @NonNull
    public Integer getId() {
        return Hook.id.getNonNull(this);
    }

    public void setId(@NonNull final Integer id) {
        Hook.id.set(this, id);
    }

    @NonNull
    public Map<String, String> getFormattedObject() {
        return Hook.formattedObject.getNonNull(this);
    }

    public void setFormattedObject(@NonNull final Map<String, String> formattedObject) {
        Hook.formattedObject.set(this, formattedObject);
    }

    @NonNull
    public Environment getEnvironment() {
        return Hook.environment.getNonNull(this);
    }

    public void setEnvironment(@NonNull final Environment environment) {
        Hook.environment.set(this, environment);
    }

    @Nullable
    public String getResult() {
        return Hook.result.get(this);
    }

    public void setResult(@NonNull final String result) {
        Hook.result.set(this, result);
    }

    public @interface Hook {
        ContextHook<Integer> id = hook("id");
        ContextHook<Map<String, String>> formattedObject = hook("formattedObject");
        ContextHook<Environment> environment = hook("environment");
        ContextHook<String> result = hook("result");
    }
}
