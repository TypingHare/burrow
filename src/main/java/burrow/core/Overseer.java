package burrow.core;

import burrow.chain.Chain;
import burrow.chain.Context;
import burrow.core.chain.*;
import burrow.core.chamber.Chamber;
import burrow.core.chamber.ChamberModule;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public final class Overseer extends ChamberModule {
    private final UpdateEntryChain createEntryChain = new UpdateEntryChain();
    private final UpdateEntryChain registerEntryChain = new UpdateEntryChain();
    private final UpdateEntryChain setEntryChain = new UpdateEntryChain();
    private final UnsetPropertiesChain unsetEntryChain = new UnsetPropertiesChain();
    private final UpdateEntryChain deleteEntryChain = new UpdateEntryChain();
    private final ToEntryObjectChain toEntryObjectChain = new ToEntryObjectChain();
    private final CommandProcessChain commandProcessChain = new CommandProcessChain();
    private final FormatEntryChain formatEntryChain = new FormatEntryChain();

    private final List<Chain<? extends Context, ?>> chainList = new ArrayList<>();

    public Overseer(@NonNull final Chamber chamber) {
        super(chamber);
        chainList.add(createEntryChain);
    }

    @NonNull
    public List<Chain<? extends Context, ?>> getChainList() {
        return chainList;
    }

    @NonNull
    public UpdateEntryChain getCreateEntryChain() {
        return createEntryChain;
    }

    @NonNull
    public UpdateEntryChain getRegisterEntryChain() {
        return registerEntryChain;
    }

    @NonNull
    public UpdateEntryChain getSetEntryChain() {
        return setEntryChain;
    }

    @NonNull
    public UnsetPropertiesChain getUnsetEntryChain() {
        return unsetEntryChain;
    }

    @NonNull
    public UpdateEntryChain getDeleteEntryChain() {
        return deleteEntryChain;
    }

    @NonNull
    public ToEntryObjectChain getToEntryObjectChain() {
        return toEntryObjectChain;
    }

    @NonNull
    public CommandProcessChain getCommandProcessChain() {
        return commandProcessChain;
    }

    @NonNull
    public FormatEntryChain getFormatEntryChain() {
        return formatEntryChain;
    }
}
