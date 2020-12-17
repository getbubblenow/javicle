package jvc.model.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import jvc.model.JFileExtension;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum JTrackType {

    general (null, null),
    audio   (JFileExtension.flac, "a"),
    video   (JFileExtension.mp4, "v"),
    image   (JFileExtension.png, null),
    subtitle(JFileExtension.png, "s"),
    data    (JFileExtension.png, "d"),
    other   (null, null);

    @JsonCreator public static JTrackType fromString(String val) { return valueOf(val.toLowerCase()); }

    private final JFileExtension ext;
    public JFileExtension ext() { return ext; }

    private final String ffmpegType;
    public String ffmpegType() { return ffmpegType; }
    public boolean hasFfmpegType() { return ffmpegType != null; }

}
