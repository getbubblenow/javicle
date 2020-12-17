package jvcl.operation;

import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static jvcl.service.Toolbox.evalBig;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public interface HasStartAndEnd {

    String getStart ();
    default boolean hasStartTime () { return !empty(getStart()); }

    String getEnd ();
    default boolean hasEndTime () { return !empty(getEnd()); }

    default BigDecimal getStartTime(Map<String, Object> ctx, JsEngine js) {
        return evalBig(getStart(), ctx, js, ZERO);
    }

    default BigDecimal getEndTime(Map<String, Object> ctx, JsEngine js) {
        return getEndTime(ctx, js, null);
    }

    default BigDecimal getEndTime(Map<String, Object> ctx, JsEngine js, BigDecimal defaultValue) {
        return evalBig(getEnd(), ctx, js, defaultValue);
    }

}
