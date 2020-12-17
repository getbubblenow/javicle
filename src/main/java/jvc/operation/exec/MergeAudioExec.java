package jvc.operation.exec;

import jvc.model.JAsset;
import jvc.model.JFileExtension;
import jvc.model.operation.JSingleOperationContext;
import jvc.operation.MergeAudioOperation;
import jvc.service.AssetManager;
import jvc.service.Toolbox;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.TempDir;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.io.FileUtil.*;

@Slf4j
public class MergeAudioExec extends SingleOrMultiSourceExecBase<MergeAudioOperation> {

    public static final String PAD_WITH_SILENCE_TEMPLATE
            = "cd {{{tempDir}}} && {{{ffmpeg}}} -f concat -i {{{playlist.path}}} -codec copy -y {{{padded}}}";

    public static final String MERGE_AUDIO_TEMPLATE
            = "{{{ffmpeg}}} -i {{{source.path}}} -i {{audio.path}} -filter_complex \""
            + "[0:a][1:a] amix=inputs=2 [merged]"
            + "\" "
            + "-map 0:v -map \"[merged]\" -c:v copy "
            + "-y {{{output.path}}}";

    @Override protected String getProcessTemplate() { return MERGE_AUDIO_TEMPLATE; }

    @Override public void operate(MergeAudioOperation op, Toolbox toolbox, AssetManager assetManager) {
        final JSingleOperationContext opCtx = op.getSingleInputContext(assetManager);
        final JAsset source = opCtx.source;
        final JAsset output = opCtx.output;
        final JFileExtension formatType = opCtx.formatType;

        final JAsset audio = assetManager.resolve(op.getInsert());

        final StandardJsEngine js = toolbox.getJs();
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());
        ctx.put("source", source);
        ctx.put("audio", audio);

        final BigDecimal insertAt = op.getAt(ctx, js);
        ctx.put("start", insertAt);

        if (insertAt.compareTo(ZERO) > 0) {
            final JAsset silence = createSilence(op, toolbox, assetManager, insertAt, audio);
            final JAsset padded = padWithSilence(op, toolbox, assetManager, audio, silence);
            ctx.put("audio", padded);
        }

        operate(op, toolbox, assetManager, source, output, formatType, ctx);
    }

    protected JAsset padWithSilence(MergeAudioOperation op,
                                    Toolbox toolbox,
                                    AssetManager assetManager,
                                    JAsset audio,
                                    JAsset silence) {
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("ffmpeg", toolbox.getFfmpeg());

        final JFileExtension ext = audio.getFormat().getFileExtension();
        final JAsset padded = new JAsset().setPath(abs(assetManager.assetPath(op, audio, ext)));
        final String paddedName = basename(padded.getPath());
        ctx.put("padded", paddedName);

        // create a temp dir for concat, it really likes to have everything in the same directory
        @Cleanup("delete") final TempDir tempDir = new TempDir(assetManager.getScratchDir());
        ctx.put("tempDir", abs(tempDir));

        final String silenceName = basename(silence.getPath());
        final String audioName = basename(audio.getPath());

        // write playlist
        final File playlistFile = new File(tempDir, "playlist.txt");
        toFileOrDie(playlistFile, "file "+ silenceName +"\nfile "+ audioName);

        // copy audio and silence assets to temp dir
        copyFile(new File(silence.getPath()), new File(tempDir, silenceName));
        copyFile(new File(audio.getPath()), new File(tempDir, audioName));

        final JAsset playlist = new JAsset().setPath(abs(playlistFile));
        ctx.put("playlist", playlist);

        final String script = renderScript(toolbox, ctx, PAD_WITH_SILENCE_TEMPLATE);

        log.debug("padWithSilence: running script: "+script);
        final String scriptOutput = exec(script, op.isNoExec());

        final File outputFile = new File(tempDir, paddedName);
        if (!outputFile.exists()) return die("padWithSilence: output file not found: "+abs(outputFile));
        copyFile(outputFile, new File(abs(padded.getPath())));

        log.debug("padWithSilence: command output: "+scriptOutput);

        return padded;
    }

}
