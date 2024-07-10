package burrow.furniture.hoard.chain;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.core.common.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FormattedObjectToStringContext extends Context {
    @NotNull
    public Integer getId() {
        return Hook.id.getNonNull(this);
    }

    public void setId(@NotNull final Integer id) {
        Hook.id.set(this, id);
    }

    @NotNull
    public Map<String, String> getFormattedObject() {
        return Hook.formattedObject.getNonNull(this);
    }

    public void setFormattedObject(@NotNull final Map<String, String> formattedObject) {
        Hook.formattedObject.set(this, formattedObject);
    }

    @NotNull
    public Environment getEnvironment() {
        return Hook.environment.getNonNull(this);
    }

    public void setEnvironment(@NotNull final Environment environment) {
        Hook.environment.set(this, environment);
    }

    @Nullable
    public String getResult() {
        return Hook.result.get(this);
    }

    public void setResult(@NotNull final String result) {
        Hook.result.set(this, result);
    }

    public @interface Hook {
        ContextHook<Integer> id = hook("id");
        ContextHook<Map<String, String>> formattedObject = hook("formattedObject");
        ContextHook<Environment> environment = hook("environment");
        ContextHook<String> result = hook("result");
    }
}
