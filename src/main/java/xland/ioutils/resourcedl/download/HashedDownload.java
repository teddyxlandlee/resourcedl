package xland.ioutils.resourcedl.download;

import xland.ioutils.resourcedl.util.IOUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

public final class HashedDownload implements IOUtils.IORunnable {
    private final URI rootUri;
    private final Path output;
    private final String hash;
    private final UriHashRule uriHashRule;

    public HashedDownload(URI rootUri, Path output, String hash, UriHashRule uriHashRule) {
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
        IOUtils.download(getUrl(), output);
    }

    @Override
    public void runIo() throws IOException {
        this.download();
    }

    public static HashedDownload fromProperties(Properties properties)
            throws IllegalArgumentException, NullPointerException {
        URI rootUri = URI.create(Objects.requireNonNull(properties.getProperty("root")));
        Path output = Paths.get(Objects.requireNonNull(properties.getProperty("output")));
        String hash = properties.getProperty("hash");
        Objects.requireNonNull(hash);
        UriHashRule uriHashRule = UriHashRule.fromDesc(properties.getProperty("rule"));
        return new HashedDownload(rootUri, output, hash, uriHashRule);
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
