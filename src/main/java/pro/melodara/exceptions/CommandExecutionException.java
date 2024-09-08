package pro.melodara.exceptions;

public class CommandExecutionException extends RuntimeException {
    private String errorCode = null;

    public CommandExecutionException(String errorDescription) {
        super(errorDescription);
    }

    public CommandExecutionException(String errorDescription, String errorCode) {
        super(errorDescription);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
