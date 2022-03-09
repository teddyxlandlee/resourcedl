package xland.ioutils.resourcedl.multifile.json;

import mjson.Json;
import xland.ioutils.resourcedl.util.IOUtils;
import xland.ioutils.resourcedl.util.spi.MultiFileDownloadProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
}
