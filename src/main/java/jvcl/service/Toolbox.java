package jvcl.service;

import com.github.jknack.handlebars.Handlebars;
import jvcl.model.JAsset;
import jvcl.model.JsObjectView;
import jvcl.model.info.JMediaInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.handlebars.HandlebarsUtil;
import org.cobbzilla.util.io.FileUtil;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.math.RoundingMode.HALF_EVEN;
import static org.cobbzilla.util.daemon.ZillaRuntime.*;
import static org.cobbzilla.util.io.FileUtil.abs;
import static org.cobbzilla.util.io.FileUtil.replaceExt;
import static org.cobbzilla.util.json.JsonUtil.FULL_MAPPER_ALLOW_UNKNOWN_FIELDS;
import static org.cobbzilla.util.json.JsonUtil.json;
import static org.cobbzilla.util.system.CommandShell.execScript;
import static org.cobbzilla.util.time.TimeUtil.parseDuration;

@Slf4j
public class Toolbox {

    public static final Toolbox DEFAULT_TOOLBOX = new Toolbox();

    @Getter(lazy=true) private final Handlebars handlebars = initHandlebars();

    @Getter(lazy=true) private final StandardJsEngine js = new StandardJsEngine();

    public static BigDecimal getDuration(String t) {
        return big(parseDuration(t)).divide(big(1000), HALF_EVEN);
    }

    public static Map<String, Object> jsContext(Map<String, Object> ctx) {
        final Map<String, Object> jsCtx = new HashMap<>();
        for (Map.Entry<String, Object> entry : ctx.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof JsObjectView) {
                jsCtx.put(entry.getKey(), ((JsObjectView) value).toJs());
            } else {
                jsCtx.put(entry.getKey(), value);
            }
        }
        return jsCtx;
    }

    private Handlebars initHandlebars() {
        final Handlebars hbs = new Handlebars(new HandlebarsUtil(Toolbox.class.getSimpleName()));
        HandlebarsUtil.registerUtilityHelpers(hbs);
        HandlebarsUtil.registerDateHelpers(hbs);
        HandlebarsUtil.registerJavaScriptHelper(hbs, StandardJsEngine::new);
        return hbs;
    }

    @Getter(lazy=true) private final String ffmpeg = initFfmpeg();
    private String initFfmpeg() { return loadPath("ffmpeg"); }

    @Getter(lazy=true) private final String mediainfo = initMediainfo();
    private String initMediainfo() { return loadPath("mediainfo"); }

    private static String loadPath(String p) {
        try {
            final String path = execScript("which "+p);
            if (empty(path)) return die("'which "+p+"' returned empty string");
            return path.trim();
        } catch (Exception e) {
            return die("loadPath("+p+"): "+shortError(e));
        }
    }

    private final Map<String, JMediaInfo> infoCache = new ConcurrentHashMap<>();

    public JMediaInfo getInfo(JAsset asset) {
        if (!asset.hasPath()) return die("getInfo: no path for asset: "+asset);
        final String infoName = replaceExt(asset.getPath(), ".json");
        final File infoFile = new File(infoName);
        final String infoPath = abs(infoFile);
        if (!infoFile.exists() || infoFile.length() == 0) {
            execScript(getMediainfo() + " --Output=JSON " + abs(asset.getPath())+" > "+infoPath);
        }
        if (!infoFile.exists() || infoFile.length() == 0) {
            return die("getInfo: info file was not created or was empty: "+infoPath);
        }
        return infoCache.computeIfAbsent(infoPath, p -> {
            try {
                return json(FileUtil.toStringOrDie(infoFile), JMediaInfo.class, FULL_MAPPER_ALLOW_UNKNOWN_FIELDS);
            } catch (Exception e) {
                return die("getInfo: "+shortError(e), e);
            }
        });
    }
}
