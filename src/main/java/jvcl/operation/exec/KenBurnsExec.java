package jvcl.operation.exec;


import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.operation.JSingleOperationContext;
import jvcl.operation.KenBurnsOperation;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KenBurnsExec extends ExecBase<KenBurnsOperation> {

    public static final String KEN_BURNS_TEMPLATE
            = "{{{ffmpeg}}} -i {{{source.path}}} -filter_complex \""
            + "scale={{expr width '*' 16}}x{{expr height '*' 16}}, "
            + "zoompan="
            + "z='min(zoom*{{zoomIncrementFactor}},{{zoom}})':"
            + "d={{duration}}:"
            + "x='if(gte(zoom,{{zoom}}),x,x+{{deltaX}}/a)':"
            + "y='if(gte(zoom,{{zoom}}),y,y+{{deltaY}})':"
            + "s={{width}}x{{height}}"
            + "\" -y {{{output.path}}}";

    @Override public void operate(KenBurnsOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;


    }

}
