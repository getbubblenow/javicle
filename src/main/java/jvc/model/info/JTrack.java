package jvc.model.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import static java.lang.Integer.parseInt;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.string.StringUtil.isOnlyDigits;

public class JTrack {

    @JsonProperty("@type") @Getter @Setter private String type;
    public JTrackType type() {
        try {
            return JTrackType.fromString(type);
        } catch (Exception e) {
            return JTrackType.other;
        }
    }
    public boolean audio() { return type() == JTrackType.audio; }
    public boolean video() { return type() == JTrackType.video; }
    public boolean image() { return type() == JTrackType.image; }
    public boolean audioOrVideo() { return audio() || video(); }
    public boolean imageOrVideo() { return image() || video(); }

    @JsonProperty("ID") @Getter @Setter private String id;
    @JsonProperty("StreamOrder") @Getter @Setter private String streamOrder;
    @JsonProperty("VideoCount") @Getter @Setter private String videoCount;
    @JsonProperty("AudioCount") @Getter @Setter private String audioCount;

    @JsonProperty("FileExtension") @Getter @Setter private String fileExtension;
    public boolean hasFileExtension () { return !empty(fileExtension); }

    @JsonProperty("Format") @Getter @Setter private String format;
    public boolean hasFormat () { return !empty(format); }

    @JsonProperty("Format_AdditionalFeatures") @Getter @Setter private String formatAdditionalFeatures;
    @JsonProperty("Format_Profile") @Getter @Setter private String formatProfile;
    @JsonProperty("Format_Level") @Getter @Setter private String formatLevel;
    @JsonProperty("Channels") @Getter @Setter private String channels;
    @JsonProperty("ChannelPositions") @Getter @Setter private String channelPositions;
    @JsonProperty("ChannelLayout") @Getter @Setter private String channelLayout;

    public String channelLayout () {
        if (!empty(channelLayout)) {
            return channelLayout.equals("L R") ? "stereo": channelLayout;
        }
        if (!empty(channels)) {
            if (isOnlyDigits(channels)) {
                switch (parseInt(channels)) {
                    case 1: return "mono";
                    case 2: return "stereo";
                }
            }
            return channels;
        }
        return null;
    }

    @JsonProperty("SamplesPerFrame") @Getter @Setter private String samplesPerFrame;

    @JsonProperty("SamplingRate") @Getter @Setter private String samplingRate;
    public boolean hasSamplingRate () { return !empty(samplingRate); }

    @JsonProperty("SamplingCount") @Getter @Setter private String samplingCount;
    @JsonProperty("Compression_Mode") @Getter @Setter private String compressionMode;
    @JsonProperty("BitRate") @Getter @Setter private String bitrate;

    @JsonProperty("Width") @Getter @Setter private String width;
    public Integer width () { return parseInt(width); }
    public boolean hasWidth () { return !empty(width); }

    @JsonProperty("Height") @Getter @Setter private String height;
    public Integer height () { return parseInt(height); }
    public boolean hasHeight () { return !empty(height); }

    @JsonProperty("Sampled_Width") @Getter @Setter private String sampledWidth;
    @JsonProperty("Sampled_Height") @Getter @Setter private String sampledHeight;
    @JsonProperty("PixelAspectRatio") @Getter @Setter private String pixelAspectRatio;
    @JsonProperty("DisplayAspectRatio") @Getter @Setter private String displayAspectRatio;
    @JsonProperty("Rotation") @Getter @Setter private String rotation;
    @JsonProperty("CodecID") @Getter @Setter private String codecID;
    @JsonProperty("CodecID_Compatible") @Getter @Setter private String codecIDCompatible;
    @JsonProperty("FileSize") @Getter @Setter private String fileSize;

    @JsonProperty("Duration") @Getter @Setter private String duration;
    public boolean hasDuration () { return !empty(duration); }

    @JsonProperty("OverallBitRate_Mode") @Getter @Setter private String overallBitRateMode;
    @JsonProperty("OverallBitRate") @Getter @Setter private String overallBitRate;
    @JsonProperty("FrameRate") @Getter @Setter private String frameRate;
    @JsonProperty("FrameRate_Mode") @Getter @Setter private String frameRateMode;
    @JsonProperty("FrameRate_Minimum") @Getter @Setter private String frameRateMinimum;
    @JsonProperty("FrameRate_Maximum") @Getter @Setter private String frameRateMaximum;
    @JsonProperty("FrameCount") @Getter @Setter private String frameCount;
    @JsonProperty("ColorSpace") @Getter @Setter private String colorSpace;
    @JsonProperty("ChromaSubsampling") @Getter @Setter private String chromaSubsampling;
    @JsonProperty("BitDepth") @Getter @Setter private String bitDepth;
    @JsonProperty("ScanType") @Getter @Setter private String scanType;
    @JsonProperty("StreamSize") @Getter @Setter private String streamSize;
    @JsonProperty("StreamSize_Proportion") @Getter @Setter private String streamSizeProperties;
    @JsonProperty("HeaderSize") @Getter @Setter private String headerSize;
    @JsonProperty("DataSize") @Getter @Setter private String dataSize;
    @JsonProperty("FooterSize") @Getter @Setter private String footerSize;
    @JsonProperty("IsStreamable") @Getter @Setter private String isStreamable;
    @JsonProperty("Title") @Getter @Setter private String title;
    @JsonProperty("Movie") @Getter @Setter private String movie;
    @JsonProperty("Encoded_Date") @Getter @Setter private String encodedDate;
    @JsonProperty("Encoded_Library") @Getter @Setter private String encodedLibrary;
    @JsonProperty("Encoded_Library_Name") @Getter @Setter private String encodedLibraryName;
    @JsonProperty("Encoded_Library_Version") @Getter @Setter private String encodedLibraryVersion;
    @JsonProperty("Encoded_Library_Settings") @Getter @Setter private String encodedLibrarySettings;
    @JsonProperty("Tagged_date") @Getter @Setter private String taggedDate;
    @JsonProperty("File_Modified_Date") @Getter @Setter private String fileModifiedDate;
    @JsonProperty("File_Modified_Date_Local") @Getter @Setter private String fileModifiedDateLocal;
    @JsonProperty("Encoded_Application") @Getter @Setter private String encodedApplication;
    @JsonProperty("Comment") @Getter @Setter private String comment;
    @Getter @Setter private JsonNode extra;

}
