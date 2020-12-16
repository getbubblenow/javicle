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
public class OverlayOperation extends JSingleSourceOperation {

    @Getter @Setter private OverlayConfig overlay;

    @Getter @Setter private String start;
    public BigDecimal getStartTime(Map<String, Object> ctx, JsEngine js) {
        return evalBig(start, ctx, js, ZERO);
    }

    @Getter @Setter private String end;
    public BigDecimal getEndTime(Map<String, Object> ctx, JsEngine js) {
        return evalBig(end, ctx, js);
    }

    public static class OverlayConfig implements HasWidthAndHeight {
        @Getter @Setter private String source;

        @Getter @Setter private String start;
        public BigDecimal getStartTime(Map<String, Object> ctx, JsEngine js) {
            return evalBig(start, ctx, js, ZERO);
        }

        @Getter @Setter private String end;
        public boolean hasEndTime () { return !empty(end); }
        public BigDecimal getEndTime(Map<String, Object> ctx, JsEngine js) {
            return evalBig(end, ctx, js);
        }

        @Getter @Setter private String width;
        @Getter @Setter private String height;

        @Getter @Setter private String x;
        public boolean hasX () { return !empty(x); }
        public BigDecimal getX(Map<String, Object> ctx, JsEngine js) { return evalBig(x, ctx, js); }

        @Getter @Setter private String y;
        public boolean hasY () { return !empty(y); }
        public BigDecimal getY(Map<String, Object> ctx, JsEngine js) { return evalBig(y, ctx, js); }

    }
}
