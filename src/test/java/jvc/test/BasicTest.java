package jvc.test;

import jvc.main.Jvc;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.cobbzilla.util.daemon.ZillaRuntime.shortError;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.basename;
import static org.cobbzilla.util.io.StreamUtil.loadResourceAsStream;
import static org.cobbzilla.util.io.StreamUtil.stream2file;
import static org.junit.Assert.fail;

@Slf4j
public class BasicTest {

    @Test public void testSplitConcatTrimScaleLetterbox  () {
        runSpec("tests/test_split.jvc");
        runSpec("tests/test_concat.jvc");
        runSpec("tests/test_trim.jvc");
        runSpec("tests/test_scale.jvc");
        runSpec("tests/test_letterbox.jvc");
    }

    @Test public void testOverlay     () { runSpec("tests/test_overlay.jvc"); }
    @Test public void testKenBurns    () { runSpec("tests/test_ken_burns.jvc"); }
    @Test public void testRemoveTrack () { runSpec("tests/test_remove_track.jvc"); }
    @Test public void testMergeAudio  () { runSpec("tests/test_merge_audio.jvc"); }
    @Test public void testAddSilence  () { runSpec("tests/test_add_silence.jvc"); }

    private void runSpec(String specPath) {
        try {
            @Cleanup("delete") final File specFile = stream2file(loadResourceAsStream(specPath));
            JvcTest.main(new String[]{abs(specFile)});
        } catch (Exception e) {
            fail("runSpec("+specPath+") failed: "+shortError(e)+"\n"+getStackTrace(e));
        }
        log.info("runSpec: completed successfully: "+basename(specPath));
    }

    public static class JvcTest extends Jvc {
        @Override protected boolean exit() { return false; }
        public static void main (String[] args) { main(JvcTest.class, args); }
    }

}
