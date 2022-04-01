package xland.ioutils.resourcedl.util;

import xland.ioutils.resourcedl.download.HashedDownload;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public interface UrlProvider {
    URL getUrl() throws MalformedURLException;

    Path getOutput();

    /*@Nullable*/
    HashedDownload asHashedDownload();

    default String getRelativeOutput(/*@Nullable*/ Path root) {
        // TODO: for compatibility reasons, only use the filename without path by default
        Path output = getOutput();
        Path p = output.getFileName();
        if (root != null) {
            try {
                p = output.relativize(root);
            } catch (IllegalArgumentException ignore) {}
        }
        return p.toString();
    }
}
