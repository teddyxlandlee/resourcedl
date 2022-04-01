package xland.ioutils.resourcedl.multifile.json;

import mjson.Json;
import xland.ioutils.resourcedl.download.HashedDownload;
import xland.ioutils.resourcedl.download.UriHashRule;
import xland.ioutils.resourcedl.util.IOUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JsonMultiFileDownload {
    //static final Logger LOGGER = LoggerFactory.getLogger("JsonMultiFileDownload");

    private final Json root;
    private final Path output;

    public JsonMultiFileDownload(Json root, Path output) {
        this.root = root;
        this.output = output;
    }

    public List<IOUtils.IORunnable> read() throws Json.MalformedJsonException {
        if (root.isArray()) return handleArray(root);
        if (root.isObject()) return handleObject(root);
        throw new Json.MalformedJsonException(root.toString(16));
    }

    private List<IOUtils.IORunnable> handleArray(Json arr) {
        List<IOUtils.IORunnable> list = new ArrayList<>();
        for (Json json : arr.asJsonList()) {
            if (json.isObject()) handleObject0(json, list);
            else if (json.isString()) handleString0(json.asString(), list);
        }
        return list;
    }

    private List<IOUtils.IORunnable> handleObject(Json obj) {
        List<IOUtils.IORunnable> list = new ArrayList<>();
        handleObject0(obj, list);
        return list;
    }

    private void handleString0(String s, Collection<IOUtils.IORunnable> c) {
        URL url = getUrl(s);
        c.add(new CommonDownload(url, getFilename(url, null)));
    }

    private void handleObject0(Json obj, Collection<IOUtils.IORunnable> c) {
        if (hasString(obj, "raw")) {
            URL url = getUrl(obj.at("raw").asString());
            String output = hasString(obj, "output") ?
                    obj.at("output").asString() : null;
            c.add(new CommonDownload(url, getFilename(url, output)));
            return;
        }
        String s = getString(obj, "root");
        URI root = getUri(s);
        s = getString(obj, "rule");
        UriHashRule rule = s == null ? UriHashRule.sha1(true, false) :
                UriHashRule.fromDesc(s);
        Json arr = assertArray(obj.at("downloads"));
        for (Json e : arr.asJsonList()) {
            assertObj(e);
            String hash = getString(e, "hash");
            Path output = hasString(e, "output") ?
                    getFilename(null, e.at("output").asString()) :
                    rule.getFilePath(this.output, hash);
            c.add(new HashedDownload(root, output,
                    hash, rule));
        }
    }

    private static URI getUri(String s) throws Json.MalformedJsonException {
        try {
            return new URI(s);
        } catch (URISyntaxException e) {
            throw new Json.MalformedJsonException("Expected URI, got " + s);
        }
    }

    Path getFilename(URL url, String preConf) {
        if (preConf != null) return output.resolve(preConf);
        preConf = url.getPath();
        if (preConf.isEmpty()) preConf = url.toString();
        preConf = preConf.substring(preConf.lastIndexOf('/') +1)
                .substring(preConf.lastIndexOf('\\')+1);
        return output.resolve(preConf);
    }

    private static void assertObj(Json obj) throws Json.MalformedJsonException {
        if (!obj.isObject())
            throw new Json.MalformedJsonException("Expected object, got " + obj.toString(16));
    }

    private static Json assertArray(Json arr) throws Json.MalformedJsonException {
        if (!arr.isArray())
            throw new Json.MalformedJsonException("Expected array, got " + arr.toString(16));
        return arr;
    }

    private static URL getUrl(String s) throws Json.MalformedJsonException {
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw new Json.MalformedJsonException("Malformed URL: " + s);
        }
    }

    private static String getString(Json obj, String key) throws Json.MalformedJsonException {
        if (hasString(obj, key)) return obj.at(key).asString();
        throw new Json.MalformedJsonException("object " + obj.toString(16) +
                ": requires key " + key);
    }

    private static boolean hasString(Json obj, String key) {
        return obj.has(key) && obj.at(key).isString();
    }
}
