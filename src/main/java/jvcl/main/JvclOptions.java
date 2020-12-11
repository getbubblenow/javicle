package jvcl.main;

import jvcl.model.JSpec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.main.BaseMainOptions;
import org.kohsuke.args4j.Option;

import java.io.File;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.readStdin;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.toStringOrDie;
import static org.cobbzilla.util.json.JsonUtil.FULL_MAPPER_ALLOW_COMMENTS;
import static org.cobbzilla.util.json.JsonUtil.json;

@Slf4j
public class JvclOptions extends BaseMainOptions {

    public static final String USAGE_SPEC = "Spec file to run. Default is to read from stdin.";
    public static final String OPT_SPEC = "-f";
    public static final String LONGOPT_SPEC = "--file";
    @Option(name=OPT_SPEC, aliases=LONGOPT_SPEC, usage=USAGE_SPEC)
    @Getter @Setter private File specFile;

    public JSpec getSpec() {
        if (specFile != null && !specFile.getName().equals("-")) {
            if (!specFile.exists()) return die("File not found: "+abs(specFile));
            return json(toStringOrDie(specFile), JSpec.class, FULL_MAPPER_ALLOW_COMMENTS);
        }
        log.info("reading JVCL spec from stdin...");
        return json(readStdin(), JSpec.class);
    }

    public static final String USAGE_SCRATCH_DIR = "Scratch directory. Default is to create a temp directory under /tmp";
    public static final String OPT_SCRATCH_DIR = "-t";
    public static final String LONGOPT_SCRATCH_DIR = "--temp-dir";
    @Option(name=OPT_SCRATCH_DIR, aliases=LONGOPT_SCRATCH_DIR, usage=USAGE_SCRATCH_DIR)
    @Getter @Setter private File scratchDir;

}
