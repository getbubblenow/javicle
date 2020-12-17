package jvc.operation;

import jvc.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static jvc.service.Toolbox.evalBig;

@Slf4j
public class SplitOperation extends JSingleSourceOperation implements HasStartAndEnd {

    @Getter @Setter private String interval;
    public BigDecimal getIntervalIncr(Map<String, Object> ctx, JsEngine js) {
        return evalBig(this.interval, ctx, js);
    }

    @Getter @Setter private String start;
    @Getter @Setter private String end;

}
