package jvcl.operation;

import jvcl.model.JAsset;
import jvcl.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static jvcl.service.Toolbox.evalBig;

@Slf4j
public class SplitOperation extends JSingleSourceOperation {

    @Getter @Setter private String interval;
    public BigDecimal getIntervalIncr(Map<String, Object> ctx, JsEngine js) {
        return evalBig(this.interval, ctx, js);
    }

    @Getter @Setter private String start;
    public BigDecimal getStartTime(Map<String, Object> ctx, JsEngine js) {
        return evalBig(start, ctx, js, ZERO);
    }

    @Getter @Setter private String end;
    public BigDecimal getEndTime(JAsset asset, Map<String, Object> ctx, JsEngine js) {
        return evalBig(end, ctx, js, asset.duration());
    }

}
