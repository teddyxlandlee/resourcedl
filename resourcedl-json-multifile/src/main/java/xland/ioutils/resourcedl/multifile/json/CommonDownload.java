package xland.ioutils.resourcedl.multifile.json;

import xland.ioutils.resourcedl.util.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

final class CommonDownload implements IOUtils.IORunnable {
    private final URL from;
    private final Path to;

    public CommonDownload(URL from, Path to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void runIo() throws IOException {
        IOUtils.download(from, to);
    }
}
