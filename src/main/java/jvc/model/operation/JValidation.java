package jvc.model.operation;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.javascript.JsEngine;

import java.util.Map;

import static jvc.service.Toolbox.evalBoolean;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class JValidation {

    @Getter @Setter private String test;
    @Getter @Setter private String comment;
    public boolean hasComment () { return !empty(comment); }

    public JValidationResult eval(Map<String, Object> ctx, JsEngine js) {
        try {
            return new JValidationResult(this, evalBoolean(test, ctx, js));
        } catch (Exception e) {
            return new JValidationResult(this, e);
        }
    }

    @Override public String toString() {
        return (hasComment() ? getComment() + " - " : "") + "TEST=" + getTest();
    }

}
