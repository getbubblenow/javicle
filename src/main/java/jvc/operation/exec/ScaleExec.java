package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.ScaleOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ScaleExec extends SingleOrMultiSourceExecBase<ScaleOperation> {

    public static final String SCALE_TEMPLATE
            = "{{ffmpeg}} -i {{{source.path}}} -filter_complex \""
            + "scale={{width}}x{{height}}" +
            "\" -y {{{output.path}}}";

    @Override protected String getProcessTemplate() { return SCALE_TEMPLATE; }

    @Override public void operate(ScaleOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final StandardJsEngine js = toolbox.getJs();
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());

        if (op.hasFactor()) {
            final BigDecimal factor = op.getFactor(ctx, js);
            ctx.put("width", factor.multiply(source.getWidth()).intValue());
            ctx.put("height", factor.multiply(source.getHeight()).intValue());
        } else {
            op.setProportionalWidthAndHeight(ctx, js, source);
        }

        operate(op, toolbox, assetManager, source, output, formatType, ctx);
    }

}
