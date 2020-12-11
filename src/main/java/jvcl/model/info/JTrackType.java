package jvcl.model.info;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum JTrackType {

    general, audio, video, other;

    @JsonCreator public static JTrackType fromString(String val) { return valueOf(val.toLowerCase()); }

}
