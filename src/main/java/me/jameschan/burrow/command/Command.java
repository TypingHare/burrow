package me.jameschan.burrow.command;

import me.jameschan.burrow.context.RequestContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class Command implements Callable<Integer> {
    protected final RequestContext context;

    public Command(final RequestContext context) {
        this.context = context;
    }
}
