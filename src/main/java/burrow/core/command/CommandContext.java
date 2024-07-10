package burrow.core.command;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.core.chamber.ChamberContext;
import burrow.core.common.Environment;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public final class CommandContext extends Context {
    @NonNull
    public ChamberContext getChamberContext() {
        return Hook.chamberContext.getNonNull(this);
    }

    public void setChamberContext(@NonNull final ChamberContext chamberContext) {
        Hook.chamberContext.set(this, chamberContext);
    }

    @NonNull
    public String getCommandName() {
        return Hook.commandName.getNonNull(this);
    }

    public void setCommandName(@NonNull final String commandName) {
        Hook.commandName.set(this, commandName);
    }

    @NonNull
    public List<String> getCommandArgs() {
        return Hook.commandArgs.getNonNull(this);
    }

    public void setCommandArgs(@NonNull final List<String> commandArgs) {
        Hook.commandArgs.set(this, commandArgs);
    }

    @NonNull
    public Environment getEnvironment() {
        return Hook.environment.getNonNull(this);
    }

    public void setEnvironment(@NonNull final Environment environment) {
        Hook.environment.set(this, environment);
    }

    @NonNull
    public Integer getExitCode() {
        return Hook.exitCode.getNonNull(this);
    }

    public void setExitCode(@NonNull final Integer exitCode) {
        Hook.exitCode.set(this, exitCode);
    }

    @NonNull
    public StringBuilder getBuffer() {
        return Hook.buffer.getNonNull(this);
    }

    public void setBuffer(@NonNull final StringBuilder buffer) {
        Hook.buffer.set(this, buffer);
    }

    @Nullable
    public String getImmediateCommand() {
        return Hook.immediateCommand.get(this);
    }

    public void setImmediateCommand(@NonNull final String immediateCommand) {
        Hook.immediateCommand.set(this, immediateCommand);
    }

    public @interface Hook {
        ContextHook<ChamberContext> chamberContext = hook("chamberContext");
        ContextHook<String> commandName = hook("commandName");
        ContextHook<List<String>> commandArgs = hook("commandArgs");
        ContextHook<Environment> environment = hook("environment");
        ContextHook<Integer> exitCode = hook("exitCode");
        ContextHook<StringBuilder> buffer = hook("buffer");
        ContextHook<String> immediateCommand = hook("immediateCommand");
    }
}
