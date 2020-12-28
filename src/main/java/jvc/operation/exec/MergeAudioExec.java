package jvc.operation.exec;

import com.fasterxml.jackson.databind.JsonNode;
import jvc.model.JAsset;
import jvc.model.JStreamType;
import jvc.model.operation.JOperation;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.ConcatOperation;
import jvc.operation.MergeAudioOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.javascript.JsEngine;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.json.JsonUtil.json;

@Slf4j
public class MergeAudioExec extends SingleOrMultiSourceExecBase<MergeAudioOperation> {

    public static final String MERGE_AUDIO_TEMPLATE
            = "{{{ffmpeg}}} -i {{{source.path}}} -itsoffset {{start}} -i {{audio.path}} "

            + "-filter_complex \""
            + "{{#if source.hasAudio}}"
              // source has audio -- mix with insertion
              + "[0:a][1:{{audio.audioTrack}}] amix=inputs=2 [merged]"
            + "{{else}}"
              // source has no audio -- mix null source with insertion
              + "anullsrc=channel_layout={{audio.channelLayout}}:sample_rate={{audio.samplingRate}}:duration={{source.duration}} [silence]; "
              + "[silence][1:{{audio.audioTrack}}] amix=inputs=2 [merged]"
            + "{{/if}}"
            + "\" "
            + "-map 0:v -map \"[merged]\" -c:v copy "
            + "-y {{{output.path}}}";

    @Override protected String getProcessTemplate() { return MERGE_AUDIO_TEMPLATE; }

    @Override protected void addCommandContext(MergeAudioOperation op,
                                               JSingleOperationContext opCtx,
                                               Map<String, Object> ctx) {
        final JAsset audio = opCtx.assetManager.resolve(op.getInsert());
        final JsEngine js = opCtx.toolbox.getJs();
        final BigDecimal insertAt = op.getAt(ctx, js);
        ctx.put("start", insertAt);

        if (insertAt.compareTo(ZERO) > 0) {
            final JAsset silence = createSilence(op, opCtx.toolbox, opCtx.assetManager, insertAt, audio);
            final JAsset padded = padWithSilence(op, opCtx.toolbox, opCtx.assetManager, audio, silence);
            ctx.put("audio", padded);
        } else {
            ctx.put("audio", audio);
        }
    }

    protected JAsset padWithSilence(MergeAudioOperation op,
                                    Toolbox toolbox,
                                    AssetManager assetManager,
                                    JAsset audio,
                                    JAsset silence) {
        final JStreamType streamType = audio.audioExtension();
        final JAsset padded = new JAsset()
                .setPath(abs(assetManager.assetPath(op, audio, streamType)));

        final JOperation concat = new ConcatOperation()
                .setAudioOnly(true)
                .setSources(new String[]{silence.getName(), audio.getName()})
                .setCreates(json(json(padded), JsonNode.class))
                .setOperation("concat")
                .setExecIndex(op.getExecIndex())
                .setNoExec(op.isNoExec());
        final Map<String, Object> concatCtx
                = concat.getExec(getSpec()).operate(concat, toolbox, assetManager);

        // initialize metadata
        final JAsset result = assetManager.resolve(padded.getName());

        return result;
    }

}
