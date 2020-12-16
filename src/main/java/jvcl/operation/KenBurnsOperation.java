package jvcl.operation;

import jvcl.model.operation.JSingleSourceOperation;
import lombok.Getter;
import lombok.Setter;

public class KenBurnsOperation extends JSingleSourceOperation {

    @Getter @Setter private String zoom;
    @Getter @Setter private String duration;
    @Getter @Setter private String width;
    @Getter @Setter private String height;
    @Getter @Setter private String x;
    @Getter @Setter private String y;
    @Getter @Setter private String start;
    @Getter @Setter private String end;

}
