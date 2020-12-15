package javicle.test;

import jvcl.main.Jvcl;
import lombok.Cleanup;
import org.junit.Test;

import java.io.File;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.cobbzilla.util.daemon.ZillaRuntime.shortError;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.StreamUtil.loadResourceAsStream;
import static org.cobbzilla.util.io.StreamUtil.stream2file;
import static org.junit.Assert.fail;

public class BasicTest {

    @Test public void testSplitConcatAndTrim  () {
        runSpec("tests/test_split.jvcl");
        runSpec("tests/test_concat.jvcl");
        runSpec("tests/test_trim.jvcl");
    }

    // @Test public void test4Overlay() { runSpec("tests/test_overlay.jvcl"); }

    private void runSpec(String specPath) {
        try {
            @Cleanup("delete") final File specFile = stream2file(loadResourceAsStream(specPath));
            Jvcl.main(new String[]{abs(specFile)});
        } catch (Exception e) {
            fail("runSpec("+specPath+") failed: "+shortError(e)+"\n"+getStackTrace(e));
        }
    }

}
