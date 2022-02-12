package xland.ioutils.resourcedl.console.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xland.ioutils.resourcedl.download.HashedDownload;
import xland.ioutils.resourcedl.download.UriHashRule;
import xland.ioutils.resourcedl.util.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Properties;

public abstract class DownloadWrapper implements IOUtils.IORunnable {
    final Properties properties;

    static final Logger LOGGER = LoggerFactory.getLogger("ResourceDL Console DownloadWrapper");

    DownloadWrapper(Properties properties) {
        this.properties = properties;
    }

    private static final class UrlPrinterWrapper extends DownloadWrapper {
        UrlPrinterWrapper(Properties properties) {
            super(properties);
        }

        @Override
        public void runIo() throws IOException {
            URI rootUri = URI.create(Objects.requireNonNull(properties.getProperty("root")));
            String hash = properties.getProperty("hash");
            Objects.requireNonNull(hash);
            UriHashRule uriHashRule = UriHashRule.fromDesc(properties.getProperty("rule"));
            URL url = uriHashRule.getFileUri(rootUri, hash).toURL();
            LOGGER.info("URL: {}", url);
        }
    }

    private static final class HashedDownloadWrapper extends DownloadWrapper {
        HashedDownloadWrapper(Properties properties) {
            super(properties);
            if (!properties.containsKey("output")) {
                properties.setProperty("output", DateTimeFormatter.ISO_DATE_TIME
                        .format(LocalDateTime.now())
                        .replace(':', '.').concat(".file"));
            }
        }

        @Override
        public void runIo() throws IOException {
            HashedDownload hd = HashedDownload.fromProperties(properties);
            URL url = hd.getUrl();
            LOGGER.info("Starting to download from {} to {}",
                    url, hd.output());
            hd.runIo();
            LOGGER.info("Successfully downloaded file {} from {}",
                    hd.output(), url);
        }
    }

    public static IOUtils.IORunnable get(Properties properties) {
        if (properties.containsKey("print") && !properties.containsKey("output")) {
            return new UrlPrinterWrapper(properties);
        } else
            return new HashedDownloadWrapper(properties);
    }
}
