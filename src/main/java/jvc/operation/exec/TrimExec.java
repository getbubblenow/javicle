package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.operation.TrimOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
public class TrimExec extends SingleOrMultiSourceExecBase<TrimOperation> {

    public static final String TRIM_TEMPLATE
            = "{{ffmpeg}} -i {{{source.path}}} " +
            "-ss {{startSeconds}} " +
            "{{#exists interval}}-t {{interval}} {{/exists}}" +
            "-y {{{output.path}}}";

    @Override protected String getProcessTemplate() { return TRIM_TEMPLATE; }

    @Override protected void process(Map<String, Object> ctx,
                                     TrimOperation op,
                                     JAsset source,
                                     JAsset output,
                                     JAsset subOutput,
                                     Toolbox toolbox,
                                     AssetManager assetManager) {
        final StandardJsEngine js = toolbox.getJs();
        final BigDecimal startTime = op.getStartTime(ctx, js);
        ctx.put("startSeconds", startTime);
        if (op.hasEndTime()) ctx.put("interval", op.getEndTime(ctx, js).subtract(startTime));

        super.process(ctx, op, source, output, subOutput, toolbox, assetManager);
    }

}
