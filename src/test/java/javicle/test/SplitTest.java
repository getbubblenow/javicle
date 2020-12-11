package javicle.test;

import jvcl.main.Jvcl;
import jvcl.main.JvclOptions;
import lombok.Cleanup;
import org.junit.Test;

import java.io.File;

import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.StreamUtil.loadResourceAsStream;
import static org.cobbzilla.util.io.StreamUtil.stream2file;

public class SplitTest {

    @Test public void testSplit () throws Exception {
        @Cleanup("delete") final File specFile = stream2file(loadResourceAsStream("tests/test_split.json"));
        Jvcl.main(new String[]{JvclOptions.LONGOPT_SPEC, abs(specFile)});
    }

}
