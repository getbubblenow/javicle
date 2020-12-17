package jvcl.operation;

import jvcl.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static org.cobbzilla.util.string.StringUtil.safeShellArg;

@Slf4j
public class TrimOperation extends JSingleSourceOperation implements HasStartAndEnd {

    @Getter @Setter private String start;
    @Getter @Setter private String end;

    @Override public String shortString() { return safeShellArg("trim_" + getStart() + (hasEndTime() ? "_" + getEnd() : "")); }
    public String toString() { return getSource()+"_"+getStart()+(hasEndTime() ? "_"+getEnd() : ""); }

}
