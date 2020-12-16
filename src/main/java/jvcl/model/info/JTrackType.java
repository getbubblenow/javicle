package jvcl.model.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import jvcl.model.JFileExtension;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum JTrackType {

    general (null),
    audio   (JFileExtension.flac),
    video   (JFileExtension.mp4),
    image   (JFileExtension.png),
    other   (null);

    @JsonCreator public static JTrackType fromString(String val) { return valueOf(val.toLowerCase()); }

    private final JFileExtension ext;

    public JFileExtension ext() { return ext; }

}
