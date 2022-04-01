package xland.ioutils.resourcedl.util.spi;

import xland.ioutils.resourcedl.util.IOUtils;
import xland.ioutils.resourcedl.util.UrlProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

public interface MultiFileDownloadProvider {
    List<IOUtils.IORunnable> getDownloadEngine(Path json, Path outputDir) throws IOException;

    void writeToJson(List<UrlProvider> urlProviders, /*@Nullable*/ Path root,
                     OutputStream output) throws IOException;

}
