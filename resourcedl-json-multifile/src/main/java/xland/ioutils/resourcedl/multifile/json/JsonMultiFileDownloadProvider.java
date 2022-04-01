package xland.ioutils.resourcedl.multifile.json;

import mjson.Json;
import xland.ioutils.resourcedl.download.HashedDownload;
import xland.ioutils.resourcedl.download.UriHashRule;
import xland.ioutils.resourcedl.util.IOUtils;
import xland.ioutils.resourcedl.util.UrlProvider;
import xland.ioutils.resourcedl.util.spi.MultiFileDownloadProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonMultiFileDownloadProvider implements MultiFileDownloadProvider {
    @Override
    public List<IOUtils.IORunnable> getDownloadEngine(Path json, Path outputDir) throws IOException {
        return new JsonMultiFileDownload(getAsJson(json), outputDir).read();
    }

    public static Json getAsJson(Path path) throws IOException {
        String s = Files.newBufferedReader(path)
                .lines()
                .collect(Collectors.joining());
        return Json.read(s);
    }

    private static <K, V> void put(Map<K, List<V>> map, K key, V value) {
        if (!map.containsKey(key))
            map.put(key, new ArrayList<>());
        map.get(key).add(value);
    }

    private static <E> void increase(Map<E, Integer> filenames, E filename) {
        filenames.put(filename, filenames.getOrDefault(filename, 1));
    }

    private static String nextFilename(Map<String, Integer> filenames, String filename) {
        String s = filename;
        if (filenames.containsKey(s)) {
            s = filenames.get(s) + " - " + s;
        }
        increase(filenames, filename);
        return s;
    }

    @Override
    public void writeToJson(List<UrlProvider> urlProviders, /*@Nullable*/ Path root,
                            OutputStream os) throws IOException {
        Map<String, Integer> files = new HashMap<>();
        Json array = Json.array();
        Map<Map.Entry<URI, UriHashRule>, List<HashedDownload>> hashedDownloads
                = new HashMap<>();

        for (UrlProvider urlProvider : urlProviders) {
            HashedDownload hd = urlProvider.asHashedDownload();
            if (hd == null) {
                // use common download
                String path = nextFilename(files, urlProvider.getRelativeOutput(root));
                String url = urlProvider.getUrl().toString();
                array.add(Json.object("raw", url, "output", path));
            } else {
                Map.Entry<URI, UriHashRule> websiteInfo = hd.getWebsiteInfo();
                put(hashedDownloads, websiteInfo, hd);
            }
        }

        hashedDownloads.forEach((Map.Entry<URI, UriHashRule> info, List<HashedDownload> hd) -> {
            Json downloads = Json.array();
            for (HashedDownload h : hd) {
                String hash = h.getHash();
                String output = nextFilename(files, h.getRelativeOutput(root));
                downloads.add(Json.object("hash", hash, "output", output));
            }
            String rt = info.getKey().toString();
            String rule = info.getValue().toString();
            array.add(Json.object("root", rt, "rule", rule, "downloads", downloads));
        });

        os.write(array.toString().getBytes(StandardCharsets.UTF_8));
    }
}
