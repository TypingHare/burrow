package burrow.core.command;

import burrow.chain.Context;
import burrow.chain.ContextHook;
import burrow.core.chamber.ChamberContext;
import burrow.core.common.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CommandContext extends Context {
    @NotNull
    public ChamberContext getChamberContext() {
        return Hook.chamberContext.getNonNull(this);
    }

    public void setChamberContext(@NotNull final ChamberContext chamberContext) {
        Hook.chamberContext.set(this, chamberContext);
    }

    @NotNull
    public String getCommandName() {
        return Hook.commandName.getNonNull(this);
    }

    public void setCommandName(@NotNull final String commandName) {
        Hook.commandName.set(this, commandName);
    }

    @NotNull
    public List<String> getCommandArgs() {
        return Hook.commandArgs.getNonNull(this);
    }

    public void setCommandArgs(@NotNull final List<String> commandArgs) {
        Hook.commandArgs.set(this, commandArgs);
    }

    @NotNull
    public Environment getEnvironment() {
        return Hook.environment.getNonNull(this);
    }

    public void setEnvironment(@NotNull final Environment environment) {
        Hook.environment.set(this, environment);
    }

    @NotNull
    public Integer getExitCode() {
        return Hook.exitCode.getNonNull(this);
    }

    public void setExitCode(@NotNull final Integer exitCode) {
        Hook.exitCode.set(this, exitCode);
    }

    @NotNull
    public StringBuilder getBuffer() {
        return Hook.buffer.getNonNull(this);
    }

    public void setBuffer(@NotNull final StringBuilder buffer) {
        Hook.buffer.set(this, buffer);
    }

    @Nullable
    public String getPostCommand() {
        return Hook.postCommand.get(this);
    }

    public void setPostCommand(@NotNull final String immediateCommand) {
        Hook.postCommand.set(this, immediateCommand);
    }

    public @interface Hook {
        ContextHook<ChamberContext> chamberContext = hook("chamberContext");
        ContextHook<String> commandName = hook("commandName");
        ContextHook<List<String>> commandArgs = hook("commandArgs");
        ContextHook<Environment> environment = hook("environment");
        ContextHook<Integer> exitCode = hook("exitCode");
        ContextHook<StringBuilder> buffer = hook("buffer");
        ContextHook<String> postCommand = hook("postCommand");
    }
}
