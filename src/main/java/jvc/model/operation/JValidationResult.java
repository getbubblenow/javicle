package jvc.model.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static org.cobbzilla.util.daemon.ZillaRuntime.shortError;

@AllArgsConstructor
public class JValidationResult {

    public JValidationResult(JValidation validation, boolean pass) {
        this.validation = validation;
        this.pass = pass;
    }

    public JValidationResult(JValidation validation, Exception e) {
        this.validation = validation;
        this.pass = false;
        this.exception = e;
    }

    @Getter private final JValidation validation;

    @Getter private final boolean pass;
    public boolean passed () { return pass; }
    public boolean failed () { return !passed(); }

    @Getter @Setter private Exception exception;
    public boolean hasException () { return exception != null; }

    @Override public String toString () {
        return validation.toString()+" : "
                + (passed()
                ? "PASS"
                : hasException()
                ? shortError(exception)
                : "FAIL");
    }

}
