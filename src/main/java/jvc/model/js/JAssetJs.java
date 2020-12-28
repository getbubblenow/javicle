package jvc.model.js;

import jvc.model.JAsset;
import jvc.model.info.JMediaInfo;
import jvc.model.info.JTrack;
import lombok.Getter;
import org.cobbzilla.util.collection.ArrayUtil;

import java.math.BigDecimal;

import static jvc.model.js.JTrackJs.EMPTY_TRACKS;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class JAssetJs {

    public static final JAssetJs[] EMPTY_ASSETS = new JAssetJs[0];

    @Getter public final String path;
    @Getter public final Double duration;
    @Getter public final Integer width;
    @Getter public final Integer height;
    @Getter public final Double aspectRatio;
    @Getter public final String channelLayout;
    @Getter public final Integer samplingRate;
    @Getter public JTrackJs[] allTracks = EMPTY_TRACKS;
    @Getter public JTrackJs[] tracks = EMPTY_TRACKS;
    @Getter public JTrackJs[] videoTracks = EMPTY_TRACKS;
    @Getter public JTrackJs[] audioTracks = EMPTY_TRACKS;
    @Getter public JAssetJs[] assets = EMPTY_ASSETS;
    @Getter public final boolean hasAudio;
    @Getter public final boolean hasVideo;

    public JAssetJs(JAsset asset) {
        this.path = asset.getPath();

        final BigDecimal d = asset.duration();
        this.duration = d == null ? null : d.doubleValue();

        final BigDecimal w = asset.width();
        this.width = w == null ? null : w.intValue();

        final BigDecimal h = asset.height();
        this.height = h == null ? null : h.intValue();

        this.aspectRatio = asset.aspectRatio() == null ? Double.NaN : asset.aspectRatio().doubleValue();
        this.channelLayout = asset.hasChannelLayout() ? asset.channelLayout() : null;
        this.samplingRate = asset.hasSamplingRate() ? asset.samplingRate().intValue() : 0;

        if (asset.hasInfo()) {
            final JMediaInfo info = asset.getInfo();
            for (JTrack track : info.getMedia().getTrack()) {

                final JTrackJs trackJs = new JTrackJs(track.type().name());
                allTracks = ArrayUtil.append(allTracks, trackJs);

                if (!track.audioOrVideo()) continue;

                tracks = ArrayUtil.append(tracks, trackJs);
                switch (track.type()) {
                    case audio:
                        audioTracks = ArrayUtil.append(audioTracks, trackJs);
                        break;
                    case video:
                        videoTracks = ArrayUtil.append(videoTracks, trackJs);
                        break;
                }
            }
        }

        if (asset.hasListAssets()) {
            final JAsset[] list = asset.getList();
            this.assets = new JAssetJs[list.length];
            for (int i = 0; i < assets.length; i++) {
                this.assets[i] = new JAssetJs(list[i]);
            }
        }

        hasAudio = !empty(audioTracks);
        hasVideo = !empty(videoTracks);
    }
}
