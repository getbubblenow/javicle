package jvc.model.operation;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.javascript.JsEngine;

import java.util.Map;

public class JValidation {

    @Getter @Setter private String comment;
    @Getter @Setter private String test;

    public boolean eval(Map<String, Object> ctx, JsEngine js) {
        return js.evaluateBoolean(test, ctx);
    }

}
