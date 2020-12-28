package jvc.operation;

import jvc.model.operation.JMultiSourceOperation;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Accessors(chain=true)
public class ConcatOperation extends JMultiSourceOperation {

    @Getter @Setter private Boolean audioOnly;
    public boolean audioOnly () { return audioOnly != null && audioOnly; }

    @Getter @Setter private Boolean videoOnly;
    public boolean videoOnly () { return videoOnly != null && videoOnly; }

}
