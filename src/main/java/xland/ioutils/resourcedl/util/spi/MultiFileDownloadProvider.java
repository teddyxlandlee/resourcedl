package xland.ioutils.resourcedl.util.spi;

import xland.ioutils.resourcedl.util.IOUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface MultiFileDownloadProvider {
    List<IOUtils.IORunnable> getDownloadEngine(Path json, Path outputDir) throws IOException;
}
