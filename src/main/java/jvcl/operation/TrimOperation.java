package jvcl.operation;

import jvcl.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static jvcl.service.Toolbox.getDuration;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Slf4j
public class TrimOperation extends JSingleSourceOperation {

    @Getter @Setter private String start;
    public BigDecimal getStartTime() { return empty(start) ? BigDecimal.ZERO : getDuration(start); }

    @Getter @Setter private String end;
    public boolean hasEnd() { return !empty(end); }
    public BigDecimal getEndTime() { return getDuration(end); }

    public String shortString() { return "trim_"+getStart()+(hasEnd() ? "_"+getEnd() : ""); }
    public String toString() { return getSource()+"_"+getStart()+(hasEnd() ? "_"+getEnd() : ""); }

}
