package xland.ioutils.resourcedl.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xland.ioutils.resourcedl.util.IOUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public final class HashedDownload implements IOUtils.IORunnable {
    private final URI rootUri;
    private final Path output;
    private final String hash;
    private final UriHashRule uriHashRule;

    private static final Logger LOGGER = LoggerFactory.getLogger("HashedDownload");

    public HashedDownload(URI rootUri, Path output, String hash, UriHashRule uriHashRule) {
        Objects.requireNonNull(rootUri, "rootUri");
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(hash, "hash");
        Objects.requireNonNull(uriHashRule, "uriHashRule");
        this.rootUri = rootUri;
        this.output = output;
        this.hash = hash;
        this.uriHashRule = uriHashRule;
    }

    /**
     * @throws IndexOutOfBoundsException if any of cut indexes is out
     * of {@code hash.length()}.
     * @throws IllegalArgumentException if the hash violates URI syntax.
     */
    public URL getUrl() throws MalformedURLException {
        return uriHashRule.getFileUri(rootUri, hash).toURL();
    }

    public void download() throws IOException {
        URL url = getUrl();
        LOGGER.info("Downloading from {} to {}", url, output);
        IOUtils.download(url, output);
        LOGGER.info("Successfully downloaded {}", url);
    }

    @Override
    public void runIo() throws IOException {
        this.download();
    }

    public URI rootUri() {
        return rootUri;
    }

    public Path output() {
        return output;
    }

    public String hash() {
        return hash;
    }

    public UriHashRule uriHashRule() {
        return uriHashRule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HashedDownload that = (HashedDownload) o;
        return  Objects.equals(rootUri, that.rootUri) &&
                Objects.equals(output, that.output) &&
                Objects.equals(hash, that.hash) &&
                Objects.equals(uriHashRule, that.uriHashRule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootUri, output, hash, uriHashRule);
    }
}
