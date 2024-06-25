package burrow.core;

import burrow.chain.Chain;
import burrow.chain.Context;
import burrow.core.chain.CommandProcessChain;
import burrow.core.chain.CreateEntryChain;
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
    private final CreateEntryChain createEntryChain = new CreateEntryChain();
    private final CommandProcessChain commandProcessChain = new CommandProcessChain();

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
    public CreateEntryChain getCreateEntryChain() {
        return createEntryChain;
    }

    @NonNull
    public CommandProcessChain getCommandProcessChain() {
        return commandProcessChain;
    }
}
