package jvcl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jvcl.model.info.JTrack;
import jvcl.model.info.JTrackType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static jvcl.model.info.JTrackType.*;

@AllArgsConstructor @Slf4j
public enum JFileExtension {

    mp4        (".mp4",  video),
    mkv        (".mkv",  video),
    mp3        (".mp3",  audio),
    mpeg_audio (".mp3",  audio),
    aac        (".aac",  audio),
    flac       (".flac", audio),
    png        (".png",  image),
    jpg        (".jpg",  image),
    jpeg       (".jpeg", image),
    sub        (".sub",  subtitle),
    dat        (".dat",  data),
    txt        (".txt",  data);

    @JsonCreator public static JFileExtension fromString(String v) { return valueOf(v.toLowerCase()); }

    public static boolean isValid(String v) {
        try { fromString(v); return true; } catch (Exception ignored) {}
        return false;
    }

    private final String ext;
    public String ext() { return ext; }

    private final JTrackType mediaType;
    public JTrackType mediaType() { return mediaType; }

    public static JFileExtension fromTrack(JTrack track) {
        if (track.hasFileExtension()) {
            try {
                return fromString(track.getFileExtension());
            } catch (Exception e) {
                log.warn("fromTrack: unrecognized file extension: "+track.getFileExtension());
            }
        }
        if (track.hasFormat()) {
            try {
                return fromString(track.getFormat().replace(" ", "_"));
            } catch (Exception e) {
                log.warn("fromTrack: unrecognized format: "+track.getFormat());
            }
        }
        return null;
    }

}
