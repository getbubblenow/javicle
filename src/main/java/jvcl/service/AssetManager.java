package jvcl.service;

import jvcl.model.JAsset;
import jvcl.model.JFileExtension;
import jvcl.model.JOperation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static jvcl.model.JAsset.NULL_ASSET;
import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

public class AssetManager {

    public static final String OUTFILE_PREFIX = AssetManager.class.getSimpleName() + ".output.";

    private final Toolbox toolbox;
    private final File scratchDir;
    private final Map<String, JAsset> assets = new ConcurrentHashMap<>();

    public AssetManager(Toolbox toolbox, File scratchDir) {
        this.toolbox = toolbox;
        this.scratchDir = scratchDir;
    }

    public File sourcePath(String name) { return new File(scratchDir, OUTFILE_PREFIX + "source." + name); }

    public File assetPath(JOperation op, JAsset source, JFileExtension formatType) {
        return assetPath(op, source, formatType, null);
    }

    public File assetPath(JOperation op, JAsset source, JFileExtension formatType, Object[] args) {
        return assetPath(op, new JAsset[]{source}, formatType, args);
    }

    public File assetPath(JOperation op, JAsset[] sources, JFileExtension formatType) {
        return assetPath(op, sources, formatType, null);
    }

    public File assetPath(JOperation op, JAsset[] sources, JFileExtension formatType, Object[] args) {
        return new File(scratchDir, OUTFILE_PREFIX
                + op.hash(sources, args)
                + (!empty(args) ? "_" + args[0] + (args.length > 1 ? "_" + args[1] : "") : "")
                + formatType.ext());
    }

    public Map<String, JAsset> getAssets () {
        final Map<String, JAsset> map = new HashMap<>();
        for (Map.Entry<String, JAsset> entry : assets.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private String checkName(JAsset asset) {
        final String name = asset.getName();
        if (assets.containsKey(name)) die("defineAsset: name already defined: "+ name);
        return name;
    }

    public void defineAsset(JAsset asset) {
        final String name = checkName(asset);
        assets.put(name, asset.init(this, toolbox));
    }

    public void addOperationAsset(JAsset asset) {
        if (asset == null || asset == NULL_ASSET) return;
        final String name = checkName(asset);
        assets.put(name, asset.init(this, toolbox));
    }

    public void addOperationArrayAsset(JAsset asset) {
        if (asset == null || asset == NULL_ASSET) return;
        final String name = checkName(asset);
        assets.put(name, asset);
    }

    public void addOperationAssetSlice(JAsset asset, JAsset slice) {
        if (!assets.containsKey(asset.getName())) die("asset not found: "+asset.getName());
        final JAsset found = assets.get(asset.getName());
        found.addAsset(slice.init(this, toolbox));
    }

    public JAsset[] resolve(String[] assets) {
        final JAsset[] resolved = new JAsset[assets.length];
        for (int i=0; i<assets.length; i++) {
            resolved[i] = resolve(assets[i]);
        }
        return resolved;
    }

    public JAsset resolve(String name) {
        final JAsset asset = assets.get(name);
        return asset == null ? die("resolve("+name+")") : asset;
    }

}
