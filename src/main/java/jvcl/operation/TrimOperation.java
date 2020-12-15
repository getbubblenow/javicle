package jvcl.operation;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.JOperation;
import jvcl.service.AssetManager;
import jvcl.service.Toolbox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static jvcl.model.JAsset.json2asset;
import static jvcl.service.Toolbox.getDuration;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.io.FileUtil.*;
import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class TrimOperation extends JOperation {

    @Getter @Setter private String trim;

    @Getter @Setter private String start;
    public BigDecimal getStartTime() { return empty(start) ? BigDecimal.ZERO : getDuration(start); }

    @Getter @Setter private String end;
    public boolean hasEnd() { return !empty(end); }
    public BigDecimal getEndTime() { return getDuration(end); }

    public String shortString() { return "trim_"+getStart()+(hasEnd() ? "_"+getEnd() : ""); }
    public String toString() { return trim+"_"+getStart()+(hasEnd() ? "_"+getEnd() : ""); }

}
