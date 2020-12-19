package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.ScaleOperation;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
public class ScaleExec extends SingleOrMultiSourceExecBase<ScaleOperation> {

    public static final String SCALE_TEMPLATE
            = "{{ffmpeg}} -i {{{source.path}}} -filter_complex \""
            + "scale={{width}}x{{height}}" +
            "\" -y {{{output.path}}}";

    @Override protected String getProcessTemplate() { return SCALE_TEMPLATE; }

    @Override protected void addCommandContext(ScaleOperation op,
                                               JSingleOperationContext opCtx,
                                               Map<String, Object> ctx) {
        final StandardJsEngine js = opCtx.toolbox.getJs();
        final JAsset source = opCtx.source;
        if (op.hasFactor()) {
            final BigDecimal factor = op.getFactor(ctx, js);
            ctx.put("width", factor.multiply(source.getWidth()).intValue());
            ctx.put("height", factor.multiply(source.getHeight()).intValue());
        } else {
            op.setProportionalWidthAndHeight(ctx, js, source);
        }
    }

}
