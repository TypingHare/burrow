package burrow.furniture.shellcore;

import java.beans.JavaBean;

@JavaBean
public final class ShellResponse {
    private Integer exitCode;
    private String standardOutput;
    private String errorOutput;

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public String getStandardOutput() {
        return standardOutput;
    }

    public void setStandardOutput(final String standardOutput) {
        this.standardOutput = standardOutput;
    }

    public String getErrorOutput() {
        return errorOutput;
    }

    public void setErrorOutput(final String errorOutput) {
        this.errorOutput = errorOutput;
    }
}
