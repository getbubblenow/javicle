package jvcl.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class JMedia {

    @JsonProperty("@ref") @Getter @Setter private String ref;
    @Getter @Setter private JTrack[] track;
}
