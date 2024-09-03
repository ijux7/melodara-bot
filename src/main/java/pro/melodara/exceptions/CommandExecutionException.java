package pro.melodara.exceptions;

public class CommandExecutionException extends RuntimeException {
    private final String errorCode;
    public CommandExecutionException(String errorCode) {
        super();
        this.errorCode=errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
