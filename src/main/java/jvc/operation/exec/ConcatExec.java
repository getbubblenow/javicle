package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.JStreamType;
import jvc.model.js.JAssetJs;
import jvc.model.operation.JMultiOperationContext;
import jvc.operation.ConcatOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cobbzilla.util.io.FileUtil.abs;

@Slf4j
public class ConcatExec extends ExecBase<ConcatOperation> {

    public static final String CONCAT_RECODE_TEMPLATE_1
            // list inputs
            = "{{ffmpeg}} {{#each sources}} -i {{{this.path}}}{{/each}} "

            // filter: list inputs
            + "-filter_complex \""

            // create null sources for missing audio/video streams
            + "{{#each sources}}"
              + "{{#if anyVideo}}{{#unless hasVideo}} nullsrc=s={{firstWidth}}x{{firstHeight}}:duration={{duration}} [null_video_{{@index}}]; {{/unless}}{{/if}}"
              + "{{#if anyAudio}}{{#unless hasAudio}} anullsrc=channel_layout={{firstChannelLayout}}:sample_rate={{firstSamplingRate}}:duration={{duration}} [null_audio_{{@index}}]; {{/unless}}{{/if}}"
            + "{{/each}} "

            + "{{#each sources}}"
              + "{{#if anyVideo}}{{#if hasVideo}}[{{@index}}:{{videoTrack}}]{{else}}[null_video_{{@index}}]{{/if}}{{/if}} "
              + "{{#if anyAudio}}{{#if hasAudio}}[{{@index}}:{{audioTrack}}]{{else}}[null_audio_{{@index}}]{{/if}}{{/if}} "
            + "{{/each}} "

            // filter: concat filter them together
            + "concat=n={{sources.length}}"
            + ":v={{#if anyVideo}}1{{else}}0{{/if}}"
            + ":a={{#if anyAudio}}1{{else}}0{{/if}} "
            + "{{#if anyVideo}}[v]{{/if}} "
            + "{{#if anyAudio}}[a]{{/if}}"
            + "\" "

            // output combined result
            + "{{#if anyVideo}}-map \"[v]\"{{else}}-vn{{/if}} "
            + "{{#if anyAudio}}-map \"[a]\"{{else}}-an{{/if}} "
            + "-y {{{output.path}}}";

    @Override public Map<String, Object> operate(ConcatOperation op, Toolbox toolbox, AssetManager assetManager) {

        final JMultiOperationContext opCtx = op.getMultiInputContext(assetManager, toolbox);
        final List<JAsset> sources = opCtx.sources;
        final JAsset output = opCtx.output;
        final JStreamType streamType = opCtx.streamType;

        final File defaultOutfile = assetManager.assetPath(op, sources, streamType);
        final File path = resolveOutputPath(assetManager, output, defaultOutfile);
        if (path == null) return null;
        output.setPath(abs(path));

        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("sources", sources);

        final boolean anyVideo = !op.audioOnly() && sources.stream().anyMatch(JAsset::hasHeight);
        final boolean anyAudio = !op.videoOnly() && sources.stream().anyMatch(JAsset::hasChannelLayout);
        ctx.put("anyVideo", anyVideo);
        ctx.put("anyAudio", anyAudio);

        if (anyVideo) {
            ctx.put("firstWidth", sources.stream()
                    .map(s -> ((JAssetJs) s.toJs()))
                    .filter(a -> a.hasVideo && a.width != null)
                    .map(a -> a.width)
                    .findFirst().orElse(null));
            ctx.put("firstHeight", sources.stream()
                    .map(s -> ((JAssetJs) s.toJs()))
                    .filter(a -> a.hasVideo && a.height != null)
                    .map(a -> a.height)
                    .findFirst().orElse(null));
        }

        if (anyAudio) {
            ctx.put("firstChannelLayout", sources.stream()
                    .map(s -> ((JAssetJs) s.toJs()))
                    .filter(a -> a.hasAudio && a.channelLayout != null)
                    .map(a -> a.channelLayout)
                    .findFirst().orElse(null));
            ctx.put("firstSamplingRate", sources.stream()
                    .map(s -> ((JAssetJs) s.toJs()))
                    .filter(a -> a.hasAudio && a.samplingRate != null)
                    .map(a -> a.samplingRate)
                    .findFirst().orElse(null));
        }

        ctx.put("output", output);
        final String script = renderScript(toolbox, ctx, CONCAT_RECODE_TEMPLATE_1);

        log.debug("operate: running script: "+script);
        final String scriptOutput = exec(script, op.isNoExec());
        log.debug("operate: command output: "+scriptOutput);
        assetManager.addOperationAsset(output);
        return ctx;
    }

}
