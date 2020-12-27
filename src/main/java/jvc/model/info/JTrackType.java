package jvc.model.info;

import com.fasterxml.jackson.annotation.JsonCreator;
import jvc.model.JStreamType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum JTrackType {

    general   (null, null),
    audio     (JStreamType.flac, "a"),
    video     (JStreamType.mp4, "v"),
    image     (JStreamType.png, null),
    subtitle  (JStreamType.sub, "s"),
    other     (JStreamType.dat, "d"),
    data      (JStreamType.dat, "d"),
    attachment(null, "t");

    @JsonCreator public static JTrackType fromString(String val) { return valueOf(val.toLowerCase()); }

    private final JStreamType streamType;
    public JStreamType streamType() { return streamType; }

    private final String ffmpegType;
    public String ffmpegType() { return ffmpegType; }
    public boolean hasFfmpegType() { return ffmpegType != null; }

}
