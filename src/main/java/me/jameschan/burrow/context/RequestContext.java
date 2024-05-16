package me.jameschan.burrow.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RequestContext extends Context {
    public static final class Key {
        public static final String WORKING_DIR = "WORKING_DIR";
        public static final String COMMAND_NAME = "COMMAND_NAME";
        public static final String STATUS_CODE = "STATUS_CODE";
        public static final String BUFFER = "BUFFER";
    }

    @Autowired
    public RequestContext(final Context context) {
        set(Context.Key.ROOT_DIR, context.getRootDir());
        set(Context.Key.CHAMBER, context.getChamber());
        set(Context.Key.CONFIG_FILE_PATH, context.getConfigFilePath());
        set(Context.Key.CONFIG, context.getConfig());
    }

    public Path getWorkingDir() {
        return get(Key.WORKING_DIR, Path.class);
    }

    public String COMMAND_NAME() {
        return get(Key.COMMAND_NAME);
    }

    public Integer getStatusCode() {
        return get(Key.STATUS_CODE, Integer.class);
    }

    public StringBuffer getBuffer() {
        return get(Key.BUFFER, StringBuffer.class);
    }
}
