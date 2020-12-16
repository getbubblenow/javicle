package jvcl.operation;

import jvcl.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static jvcl.service.Toolbox.evalBig;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Slf4j
public class TrimOperation extends JSingleSourceOperation {

    @Getter @Setter private String start;
    public BigDecimal getStartTime(Map<String, Object> ctx, JsEngine js) { return evalBig(start, ctx, js, ZERO); }

    @Getter @Setter private String end;
    public boolean hasEnd() { return !empty(end); }
    public BigDecimal getEndTime(Map<String, Object> ctx, JsEngine js) { return evalBig(end, ctx, js); }

    public String shortString() { return "trim_"+getStart()+(hasEnd() ? "_"+getEnd() : ""); }
    public String toString() { return getSource()+"_"+getStart()+(hasEnd() ? "_"+getEnd() : ""); }

}
