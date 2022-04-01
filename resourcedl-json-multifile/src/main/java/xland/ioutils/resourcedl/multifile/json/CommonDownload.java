package xland.ioutils.resourcedl.multifile.json;

import xland.ioutils.resourcedl.download.HashedDownload;
import xland.ioutils.resourcedl.util.IOUtils;
import xland.ioutils.resourcedl.util.UrlProvider;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

final class CommonDownload implements IOUtils.IORunnable, UrlProvider {
    private final URL from;
    private final Path to;

    public CommonDownload(URL from, Path to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        this.from = from;
        this.to = to;
    }

    @Override
    public void runIo() throws IOException {
        IOUtils.download(from, to);
    }

    @Override
    public URL getUrl() {
        return from;
    }

    @Override
    public Path getOutput() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommonDownload that = (CommonDownload) o;
        return from.equals(that.from) && to.equals(that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return "CommonDownload{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }

    @Override
    public HashedDownload asHashedDownload() {
        return null;
    }
}
