package javicle.test;

import jvcl.main.Jvcl;
import lombok.Cleanup;
import org.junit.Test;

import java.io.File;

import static jvcl.main.JvclOptions.LONGOPT_SPEC;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.StreamUtil.loadResourceAsStream;
import static org.cobbzilla.util.io.StreamUtil.stream2file;

public class BasicTest {

    @Test public void testSplit  () { runSpec("tests/test_split.json"); }
    @Test public void testConcat () { runSpec("tests/test_concat.json"); }
    @Test public void testTrim   () { runSpec("tests/test_trim.json"); }
    @Test public void testOverlay() { runSpec("tests/test_overlay.json"); }

    private void runSpec(String specPath) {
        @Cleanup("delete") final File specFile = stream2file(loadResourceAsStream(specPath));
        Jvcl.main(new String[]{LONGOPT_SPEC, abs(specFile)});
    }

}
