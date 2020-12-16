package javicle.test;

import jvcl.main.Jvcl;
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

    @Test public void testSplitConcatTrimScale  () {
        runSpec("tests/test_split.jvcl");
        runSpec("tests/test_concat.jvcl");
        runSpec("tests/test_trim.jvcl");
        runSpec("tests/test_scale.jvcl");
    }

    @Test public void testOverlay() { runSpec("tests/test_overlay.jvcl"); }
    @Test public void testKenBurns() { runSpec("tests/test_ken_burns.jvcl"); }

    private void runSpec(String specPath) {
        try {
            @Cleanup("delete") final File specFile = stream2file(loadResourceAsStream(specPath));
            JvclTest.main(new String[]{abs(specFile)});
        } catch (Exception e) {
            fail("runSpec("+specPath+") failed: "+shortError(e)+"\n"+getStackTrace(e));
        }
        log.info("runSpec: completed successfully: "+basename(specPath));
    }

    public static class JvclTest extends Jvcl {
        @Override protected boolean exit() { return false; }
        public static void main (String[] args) { main(JvclTest.class, args); }
    }

}
