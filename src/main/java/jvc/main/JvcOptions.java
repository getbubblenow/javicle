package jvc.main;

import jvc.model.JSpec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.io.TempDir;
import org.cobbzilla.util.main.BaseMainOptions;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;

import static jvc.service.Toolbox.JSON_MAPPER;
import static org.cobbzilla.util.daemon.ZillaRuntime.*;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.toStringOrDie;
import static org.cobbzilla.util.json.JsonUtil.json;

@Slf4j
public class JvcOptions extends BaseMainOptions {

    public static final String USAGE_SPEC = "Spec file to run. If omitted, read spec from stdin.";
    @Argument(usage=USAGE_SPEC)
    @Getter @Setter private File specFile;

    public JSpec getSpec() {
        final String json;
        if (specFile != null) {
            if (!specFile.exists() || !specFile.canRead()) return die("File not found or unreadable: "+abs(specFile));
            json = toStringOrDie(specFile);
        } else {
            log.info("reading JVC spec from stdin...");
            json = readStdin();
        }
        try {
            return json(json, JSpec.class, JSON_MAPPER);
        } catch (Exception e) {
            return die("getSpec: invalid spec: "+specFile+": "+shortError(e));
        }
    }

    public static final String USAGE_SCRATCH_DIR = "Scratch directory. Default is to create a temp directory under /tmp";
    public static final String OPT_SCRATCH_DIR = "-t";
    public static final String LONGOPT_SCRATCH_DIR = "--temp-dir";
    @Option(name=OPT_SCRATCH_DIR, aliases=LONGOPT_SCRATCH_DIR, usage=USAGE_SCRATCH_DIR)
    @Getter @Setter private File scratchDir = null;
    public File scratchDir() { return scratchDir == null ? new TempDir() : scratchDir; }

    public static final String USAGE_NO_EXEC = "Don't run anything, instead print out commands that would have been run";
    public static final String OPT_NO_EXEC = "-n";
    public static final String LONGOPT_NO_EXEC = "--no-exec";
    @Option(name=OPT_NO_EXEC, aliases=LONGOPT_NO_EXEC, usage=USAGE_NO_EXEC)
    @Getter @Setter private boolean noExec = false;

}
