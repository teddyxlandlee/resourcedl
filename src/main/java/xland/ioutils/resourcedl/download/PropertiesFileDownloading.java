package xland.ioutils.resourcedl.download;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;

/**
 * Here's an example of {@link HashedDownload} properties:<code><pre>
 *     # Root URI/URL
 *     root=https\://featurehouse.github.io/resources/\
 *     # File Hash (Here: SHA-256)
 *     hash=dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f
 *     # URI Hash Rule (Has Sub-directory, split in 2, 4 and 10)
 *     rule=S2,4,10
 *     # Output filename
 *     output=example.txt
 * </pre></code>
 */
@Deprecated
public final class PropertiesFileDownloading {
    public static HashedDownload fromFile(Path path)
            throws IOException, NoSuchElementException,
                   UnsupportedOperationException {
        Properties properties = new Properties();
        properties.load(Files.newBufferedReader(path));
        return fromProperties(properties);
    }

    public static HashedDownload fromProperties(Properties properties)
            throws IllegalArgumentException, NullPointerException {
        URI rootUri = URI.create(Objects.requireNonNull(properties.getProperty("root"), "root"));
        String output1 = properties.getProperty("output");
        Path output = output1 != null ? Paths.get(output1)
                : defaultOutput();
        String hash = properties.getProperty("hash");
            Objects.requireNonNull(hash, "hash");
        String rule1 = properties.getProperty("rule");
        UriHashRule uriHashRule = rule1 != null ? UriHashRule.fromDesc(rule1)
                : UriHashRule.sha1(true, false);//R2, Minecraft-style

        return new HashedDownload(rootUri, output, hash, uriHashRule);
    }

    private static Path defaultOutput() {
        return Paths.get(DateTimeFormatter.ISO_DATE_TIME
                .format(LocalDateTime.now())
                .replace(':', '.').concat(".file"));
    }
}
