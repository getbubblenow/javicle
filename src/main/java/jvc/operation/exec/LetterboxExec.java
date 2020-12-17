package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.LetterboxOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.util.HashMap;
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

    @Override public void operate(LetterboxOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final StandardJsEngine js = toolbox.getJs();
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("source", source);

        if (!op.hasWidth() || !op.hasHeight()) {
            die("operate: both width and height must be set");
        }
        ctx.put("width", op.getWidth(ctx, js).intValue());
        ctx.put("height", op.getHeight(ctx, js).intValue());

        if (op.hasColor()) {
            ctx.put("color", safeShellArg(op.getColor()));
        } else {
            ctx.put("color", DEFAULT_LETTERBOX_COLOR);
        }

        operate(op, toolbox, assetManager, source, output, formatType, ctx);
    }

}