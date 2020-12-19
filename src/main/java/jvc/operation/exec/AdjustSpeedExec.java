package jvc.operation.exec;

import jvc.model.operation.JSingleOperationContext;
import jvc.operation.AdjustSpeedOperation;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_EVEN;
import static jvc.operation.AdjustSpeedOperation.AudioSpeed.match;
import static org.cobbzilla.util.daemon.ZillaRuntime.big;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@Slf4j
public class AdjustSpeedExec extends SingleOrMultiSourceExecBase<AdjustSpeedOperation> {

    public static final BigDecimal MINIMUM_ATEMPO = big(0.5);
    public static final BigDecimal MAXIMUM_ATEMPO = big(100);

    public static final String ADJUST_SPEED_TEMPLATE
            = "{{{ffmpeg}}} -i {{{source.path}}} "
            + "-filter_complex \""
            + "[0:v]setpts={{inverseFactor}}*PTS[v]{{#if match}};[0:a]atempo={{factor}}[a]{{/if}}"
            + "\" "
            + "-map \"[v]\" "
            + "{{#if silent}}-an{{else}}-map \"[a]\"{{/if}} "
            + "-y {{{output.path}}}";

    @Override protected String getProcessTemplate() { return ADJUST_SPEED_TEMPLATE; }

    @Override protected void addCommandContext(AdjustSpeedOperation op,
                                               JSingleOperationContext opCtx,
                                               Map<String, Object> ctx) {
        final JsEngine js = opCtx.toolbox.getJs();
        final BigDecimal factor = op.getFactor(ctx, js);

        ctx.put(op.getAudio().name(), true);
        if (op.getAudio() == match) {
            if (factor.compareTo(MINIMUM_ATEMPO) < 0) die("addCommandContext: atempo cannot be less than "+MINIMUM_ATEMPO);
            if (factor.compareTo(MAXIMUM_ATEMPO) > 0) die("addCommandContext: atempo cannot be greater than "+MAXIMUM_ATEMPO);
        }
        final BigDecimal inverseFactor = ONE.divide(factor, 8, HALF_EVEN);
        ctx.put("factor", factor);
        ctx.put("inverseFactor", inverseFactor);
    }

}
