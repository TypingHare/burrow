package me.jameschan.burrow.kernel;

import java.util.List;
import me.jameschan.burrow.kernel.command.Processor;
import me.jameschan.burrow.kernel.common.Constants;
import me.jameschan.burrow.kernel.config.Config;
import me.jameschan.burrow.kernel.config.ConfigFileNotFoundException;
import me.jameschan.burrow.kernel.context.ChamberContext;
import me.jameschan.burrow.kernel.context.RequestContext;
import me.jameschan.burrow.kernel.entry.Hoard;
import me.jameschan.burrow.kernel.furniture.Renovator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Chamber {
  private final ApplicationContext applicationContext;
  private final ChamberContext context;
  private String name;

  @Autowired
  public Chamber(final ApplicationContext applicationContext, final ChamberContext chamberContext) {
    this.applicationContext = applicationContext;
    this.context = chamberContext;
  }

  @NonNull
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @NonNull
  public ChamberContext getContext() {
    return context;
  }

  @NonNull
  public String getName() {
    return name;
  }

  public void initiate(final String name) throws ChamberInitializationException {
    context.set(ChamberContext.Key.CHAMBER, this);
    context.set(ChamberContext.Key.CONFIG, getModuleObject(Config.class));
    context.set(ChamberContext.Key.HOARD, getModuleObject(Hoard.class));
    context.set(ChamberContext.Key.RENOVATOR, getModuleObject(Renovator.class));
    context.set(ChamberContext.Key.PROCESSOR, getModuleObject(Processor.class));

    try {
      checkChamberDirectory(name);
      context.getConfig().loadFromFile();
      context.getRenovator().loadFurniture();
      context.getHoard().loadFromFile();
    } catch (final Throwable ex) {
      throw new ChamberInitializationException(ex);
    }

    this.name = name;
  }

  public void terminate() {
    context.getHoard().saveToFile();
  }

  public void restart() throws ChamberInitializationException {
    terminate();

    final var chamberShepherd = applicationContext.getBean(ChamberShepherd.class);
    chamberShepherd.initiate(this.name);
  }

  public void execute(final RequestContext requestContext, final List<String> args) {
    final var hasCommand = !args.isEmpty() && !args.getFirst().startsWith("-");
    final var commandName = hasCommand ? args.getFirst() : "";
    final var realArgs = hasCommand ? args.subList(1, args.size()) : args;

    requestContext.set(RequestContext.Key.CHAMBER_CONTEXT, context);
    requestContext.set(RequestContext.Key.COMMAND_NAME, commandName);
    requestContext.set(RequestContext.Key.BUFFER, new StringBuilder());

    final var exitCode = context.getProcessor().execute(commandName, realArgs, requestContext);
    requestContext.set(RequestContext.Key.EXIT_CODE, exitCode);
  }

  private <T extends ChamberModule> T getModuleObject(final Class<T> moduleClass) {
    return applicationContext.getBean(moduleClass, this);
  }

  private void checkChamberDirectory(final String name) throws ChamberNotFoundException {
    final var dirPath = Constants.CHAMBER_ROOT_DIR.resolve(name).normalize();
    final var file = dirPath.toFile();
    if (!file.isDirectory()) {
      throw new ChamberNotFoundException(name);
    }

    context.set(ChamberContext.Key.ROOT_DIR, dirPath);
  }
}
