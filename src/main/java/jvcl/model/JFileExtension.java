package jvcl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jvcl.model.info.JTrackType;
import lombok.AllArgsConstructor;

import static jvcl.model.info.JTrackType.*;

@AllArgsConstructor
public enum JFileExtension {

    mp4  (".mp4",  video),
    mkv  (".mkv",  video),
    mp3  (".mp3",  audio),
    aac  (".aac",  audio),
    flac (".flac", audio),
    png  (".png",  image),
    jpg  (".jpg",  image),
    jpeg (".jpeg", image);

    @JsonCreator public static JFileExtension fromString(String v) { return valueOf(v.toLowerCase()); }

    public static boolean isValid(String v) {
        try { fromString(v); return true; } catch (Exception ignored) {}
        return false;
    }

    private final String ext;
    public String ext() { return ext; }

    private final JTrackType mediaType;
    public JTrackType mediaType() { return mediaType; }

}
