package jvc.operation.exec;

import jvc.model.operation.JSingleOperationContext;
import jvc.operation.LetterboxOperation;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.string.StringUtil.safeShellArg;

@Slf4j
public class LetterboxExec extends SingleOrMultiSourceExecBase<LetterboxOperation> {

    public static final String DEFAULT_LETTERBOX_COLOR = "black";

    public static final String LETTERBOX_TEMPLATE
            = "{{ffmpeg}} -i {{{source.path}}} -filter_complex \""
            + "pad="
            + "width={{width}}:"
            + "height={{height}}:"
            + "x=({{width}}-iw*min({{width}}/iw\\,{{height}}/ih))/2:"
            + "y=({{height}}-ih*min({{width}}/iw\\,{{height}}/ih))/2:"
            + "color={{{color}}}"
            + "\" -y {{{output.path}}}";

    @Override protected String getProcessTemplate() { return LETTERBOX_TEMPLATE; }

    @Override protected void addCommandContext(LetterboxOperation op,
                                               JSingleOperationContext opCtx,
                                               Map<String, Object> ctx) {
        if (!op.hasWidth() || !op.hasHeight()) {
            die("operate: both width and height must be set");
        }
        final StandardJsEngine js = opCtx.toolbox.getJs();
        ctx.put("width", op.getWidth(ctx, js).intValue());
        ctx.put("height", op.getHeight(ctx, js).intValue());

        if (op.hasColor()) {
            ctx.put("color", safeShellArg(op.getColor()));
        } else {
            ctx.put("color", DEFAULT_LETTERBOX_COLOR);
        }
    }
}
