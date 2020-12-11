package jvcl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum JFileExtension {

    mp4 (".mp4"),
    mkv (".mkv"),
    raw (".yuv");

    @JsonCreator public static JFileExtension fromString(String v) { return valueOf(v.toLowerCase()); }

    public static boolean isValid(String v) {
        try { fromString(v); return true; } catch (Exception ignored) {}
        return false;
    }

    private final String ext;
    public String ext() { return ext; }

}
