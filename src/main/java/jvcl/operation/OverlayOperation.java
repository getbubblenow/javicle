package jvcl.operation;

import jvcl.model.JOperation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static jvcl.service.Toolbox.eval;
import static jvcl.service.Toolbox.getDuration;
import static org.cobbzilla.util.daemon.ZillaRuntime.big;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Slf4j
public class OverlayOperation extends JOperation {

    @Getter @Setter private String source;
    @Getter @Setter private OverlayConfig overlay;

    @Getter @Setter private String start;
    public BigDecimal getStartSeconds(Map<String, Object> ctx, JsEngine js) {
        return empty(start) ? BigDecimal.ZERO : getDuration(eval(start, ctx, js));
    }

    @Getter @Setter private String end;
    public BigDecimal getEndSeconds(Map<String, Object> ctx, JsEngine js) {
        return empty(end) ? BigDecimal.ZERO : getDuration(eval(end, ctx, js));
    }

    public static class OverlayConfig {
        @Getter @Setter private String source;

        @Getter @Setter private String start;
        public BigDecimal getStartTime(Map<String, Object> ctx, JsEngine js) {
            return empty(start) ? BigDecimal.ZERO : getDuration(eval(start, ctx, js));
        }

        @Getter @Setter private String end;
        public boolean hasEndTime () { return !empty(end); }
        public BigDecimal getEndTime(Map<String, Object> ctx, JsEngine js) {
            return getDuration(eval(end, ctx, js));
        }

        @Getter @Setter private String width;
        public boolean hasWidth () { return !empty(width); }
        public String getWidth(Map<String, Object> ctx, JsEngine js) { return eval(width, ctx, js); }

        @Getter @Setter private String height;
        public boolean hasHeight () { return !empty(height); }
        public String getHeight(Map<String, Object> ctx, JsEngine js) { return eval(height, ctx, js); }

        @Getter @Setter private String x;
        public boolean hasX () { return !empty(x); }
        public String getX(Map<String, Object> ctx, JsEngine js) { return eval(x, ctx, js); }

        @Getter @Setter private String y;
        public boolean hasY () { return !empty(y); }
        public String getY(Map<String, Object> ctx, JsEngine js) { return eval(y, ctx, js); }

        public BigDecimal aspectRatio () {
            return big(getWidth()).divide(big(getHeight()), RoundingMode.HALF_EVEN);
        }
    }
}
