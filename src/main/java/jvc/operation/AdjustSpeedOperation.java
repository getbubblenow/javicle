package jvc.operation;

import com.fasterxml.jackson.annotation.JsonCreator;
import jvc.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static jvc.service.Toolbox.evalBig;

public class AdjustSpeedOperation extends JSingleSourceOperation {

    @Getter @Setter private String factor;
    public BigDecimal getFactor(Map<String, Object> ctx, JsEngine js) {
        return evalBig(factor, ctx, js, ONE);
    }

    @Getter @Setter private AudioSpeed audio = AudioSpeed.silent;

    public enum AudioSpeed {
        silent, unchanged, match;
        @JsonCreator public static AudioSpeed fromString (String val) { return valueOf(val.toLowerCase()); }
    }

}
