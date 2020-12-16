package jvcl.operation;

import jvcl.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static jvcl.service.Toolbox.evalBig;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class ScaleOperation extends JSingleSourceOperation implements HasWidthAndHeight {

    @Getter @Setter private String factor;
    public boolean hasFactor () { return !empty(factor); }
    public BigDecimal getFactor(Map<String, Object> ctx, JsEngine js) { return evalBig(factor, ctx, js); }

    @Getter @Setter private String width;
    @Getter @Setter private String height;

    public String shortString(Map<String, Object> ctx, JsEngine js) {
        return "scaled_"+(hasFactor() ? getFactor(ctx, js)+"x" : getWidth(ctx, js)+"x"+getHeight(ctx, js));
    }

}
