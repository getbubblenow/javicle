package jvc.model;

import jvc.model.operation.JOperation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.collection.NameAndValue;

@NoArgsConstructor @Accessors(chain=true)
public class JSpec {

    @Getter @Setter private NameAndValue[] vars;
    @Getter @Setter private JAsset[] assets;
    @Getter @Setter private JOperation[] operations;
    @Getter @Setter private JArtifact[] artifacts;

}
