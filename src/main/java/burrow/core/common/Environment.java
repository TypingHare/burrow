package burrow.core.common;

import java.beans.JavaBean;

@JavaBean
public final class Environment {
    private String workingDirectory;
    private Integer consoleWidth;

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(final String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public Integer getConsoleWidth() {
        return consoleWidth;
    }

    public void setConsoleWidth(Integer consoleWidth) {
        this.consoleWidth = consoleWidth;
    }
}
