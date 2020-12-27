package jvc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import jvc.model.JAsset;
import jvc.model.JsObjectView;
import jvc.model.info.JMediaInfo;
import jvc.service.json.JOperationModule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.handlebars.HandlebarsUtil;
import org.cobbzilla.util.javascript.JsEngine;
import org.cobbzilla.util.javascript.StandardJsEngine;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.math.RoundingMode.HALF_EVEN;
import static jvc.model.JsObjectView.isJsObjectCollection;
import static org.cobbzilla.util.daemon.ZillaRuntime.*;
import static org.cobbzilla.util.io.FileUtil.*;
import static org.cobbzilla.util.json.JsonUtil.*;
import static org.cobbzilla.util.string.StringUtil.safeShellArg;
import static org.cobbzilla.util.system.CommandShell.execScript;

@Slf4j
public class Toolbox {

    public static final Toolbox DEFAULT_TOOLBOX = new Toolbox();

    public static final BigDecimal TWO = big(2);
    public static final int DIVISION_SCALE = 12;

    public static final ObjectMapper JSON_MAPPER = FULL_MAPPER_ALLOW_COMMENTS;

    static { JSON_MAPPER.registerModule(new JOperationModule()); }

    @Getter(lazy=true) private final Handlebars handlebars = initHandlebars();

    @Getter(lazy=true) private final JsEngine js = new StandardJsEngine();

    public static String eval(String val, Map<String, Object> ctx, JsEngine js) {
        final Map<String, Object> jsCtx = Toolbox.jsContext(ctx);
        try {
            final Object result = js.evaluate(val, jsCtx);
            return result == null ? null : result.toString();
        } catch (Exception e) {
            return die("eval: error evaluating: '"+val+"': "+shortError(e));
        }
    }

    public static boolean evalBoolean(String val, Map<String, Object> ctx, JsEngine js) {
        final Map<String, Object> jsCtx = Toolbox.jsContext(ctx);
        try {
            return js.evaluateBoolean(val, jsCtx);
        } catch (Exception e) {
            return die("eval: error evaluating: '"+val+"': "+shortError(e));
        }
    }

    public static BigDecimal evalBig(String val, Map<String, Object> ctx, JsEngine js) {
        final String resolved = eval(val, ctx, js);
        try {
            return empty(resolved) ? die("evalBig: error resolving value: '"+val+"'") : big(resolved);
        } catch (NumberFormatException nfe) {
            return evalBig(resolved, ctx, js);
        }
    }

    public static BigDecimal evalBig(String val, Map<String, Object> ctx, JsEngine js, BigDecimal defaultValue) {
        return empty(val) ? defaultValue : evalBig(val, ctx, js);
    }

    public static Map<String, Object> jsContext(Map<String, Object> ctx) {
        final Map<String, Object> jsCtx = new HashMap<>();
        for (Map.Entry<String, Object> entry : ctx.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof JsObjectView) {
                jsCtx.put(entry.getKey(), ((JsObjectView) value).toJs());

            } else if (isJsObjectCollection(value)) {
                jsCtx.put(entry.getKey(), JsObjectView.toJs((Collection) value));

            } else {
                jsCtx.put(entry.getKey(), value);
            }
        }
        return jsCtx;
    }

    public static BigDecimal divideBig(BigDecimal numerator, BigDecimal denominator) {
        return numerator.divide(denominator, DIVISION_SCALE, HALF_EVEN);
    }

    private Handlebars initHandlebars() {
        final Handlebars hbs = new Handlebars(new HandlebarsUtil(Toolbox.class.getSimpleName()));
        HandlebarsUtil.registerUtilityHelpers(hbs);
        HandlebarsUtil.registerDateHelpers(hbs);
        HandlebarsUtil.registerJavaScriptHelper(hbs, StandardJsEngine::new);
        return hbs;
    }

    @Getter(lazy=true) private final String ffmpeg = initFfmpeg();
    private String initFfmpeg() { return safeShellArg(loadPath("ffmpeg")); }

    @Getter(lazy=true) private final String mediainfo = initMediainfo();
    private String initMediainfo() { return safeShellArg(loadPath("mediainfo")); }

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
            final String mediaInfoScript = getMediainfo() + " --Output=JSON " + abs(asset.getPath()) + " > " + infoPath;
            execScript(mediaInfoScript);
        }
        if (!infoFile.exists() || infoFile.length() == 0) {
            return die("getInfo: info file was not created or was empty: "+infoPath);
        }
        return infoCache.computeIfAbsent(infoPath, p -> {
            try {
                return json(toStringOrDie(infoFile), JMediaInfo.class, FULL_MAPPER_ALLOW_UNKNOWN_FIELDS);
            } catch (Exception e) {
                return die("getInfo: "+shortError(e), e);
            }
        });
    }

}
