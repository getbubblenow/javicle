package jvcl.operation;

import jvcl.model.JAsset;
import jvcl.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static jvcl.service.Toolbox.getDuration;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Slf4j
public class SplitOperation extends JSingleSourceOperation {

    @Getter @Setter private String interval;
    public BigDecimal getIntervalIncr() { return getDuration(interval); }

    @Getter @Setter private String start;
    public BigDecimal getStartTime() { return empty(start) ? BigDecimal.ZERO : getDuration(start); }

    @Getter @Setter private String end;
    public BigDecimal getEndTime(JAsset source) { return empty(end) ? source.duration() : getDuration(end); }

}
