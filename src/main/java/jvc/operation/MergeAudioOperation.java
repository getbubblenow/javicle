package jvc.operation;

import jvc.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static jvc.service.Toolbox.evalBig;

public class MergeAudioOperation extends JSingleSourceOperation {

    @Getter @Setter private String insert;

    @Getter @Setter private String at;
    public BigDecimal getAt(Map<String, Object> ctx, JsEngine js) { return evalBig(at, ctx, js, ZERO); }

}
