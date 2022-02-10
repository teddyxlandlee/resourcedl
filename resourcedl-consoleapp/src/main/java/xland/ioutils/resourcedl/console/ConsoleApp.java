package xland.ioutils.resourcedl.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xland.ioutils.resourcedl.download.UriHashRule;
import xland.ioutils.resourcedl.util.IOUtils;
import xland.ioutils.resourcedl.util.spi.ConsoleRDAppProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <h3>Arguments: </h3><ul>
 *   <li>-D / --download: <ul>
 *     <li>-r / --root &lt;url&gt;: the root URL of the resource website</li>
 *     <li>-s / --shasum &lt;shasum&gt;</li>
 *     <li>-u / --rule &lt;hashrule&gt;: see {@link UriHashRule#fromDesc(String)},
 *     default is {@link UriHashRule#sha1(boolean, boolean) R2} (Minecraft style)</li>
 *     <li>-o / --output [filename], default {@code &lt;current-time&gt;.file}</li>
 *   </ul></li>
 *   <p>These above can be replaced with:</p>
 *   <li>&lt;path/to/properties&gt;: a properties file that contains all these above.
 *   For example: {@code <pre>
 * mode=download
 * root=https\://featurehouse.github.io/resources/
 * shasum=dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f
 * rule=S2,4,10
 * output=example.txt
 *   </pre>}
 *   </li>
 * </ul>
 */
public class ConsoleApp implements ConsoleRDAppProvider {
    static final Logger LOGGER = LoggerFactory.getLogger("ResourceDL ConsoleApp");
    private static class ArgumentParser {
        transient LinkedHashMap<String, String> map = new LinkedHashMap<>();
        final String[] args;
        static final Map<Character, String> unix2gnu; static {
            Map<Character, String> m = new Hashtable<>();
            unix2gnu = null;
        }

        ArgumentParser(String... args) {
            this.args = args;
        }

        void readArgs() {
            if (getArgType(args[0]) < 0) {
                // try parse it as path
                try {
                    Path path = Paths.get(args[0]);
                    if (Files.exists(path) && !Files.isDirectory(path)) {
                        Properties properties = new Properties();
                        properties.setProperty("rule", "R2");
                        properties.setProperty("output", calcTimeFilename());
                        properties.load(Files.newBufferedReader(path));
                    }
                } catch (IOException | InvalidPathException e) {

                }
            }

            int argSize = args.length;
            String lastArgument = null;
            for (int i = 0; i < argSize; i++) {
                String arg = args[i];
                int argType = getArgType(arg);
                switch (argType) {
                    case UNKNOWN:
                        if (lastArgument == null) {
                            LOGGER.error("Illegal argument at index {}", i);
                            System.exit(-1);
                            return;
                        } else {
                            this.putToMap(lastArgument, arg, i);
                            break;
                        }
                    case GNU:
                        lastArgument = gnuCtx(arg);
                }
            }
        }

        private void putToMap(String k, String v, int index) {
            if (map.get(k) == null)
                map.put(k, v);
            else {
                LOGGER.error("Illegal argument at index {}: too many" +
                        "arguments for `{}`", index, k);
                System.exit(-1);
            }
        }

        IOUtils.IORunnable parse() {
            throw new UnsupportedOperationException();
        }

        static String gnuCtx(String arg) {
            return arg.substring(2);
        }

        static final int GNU = 0, UNIX = 1, UNKNOWN = -1;
        static int getArgType(String arg) {
            switch (arg.length()) {
                case 0:
                case 1:
                    return UNKNOWN;
                case 2:
                    if (arg.charAt(0) != '-') return UNKNOWN;
                    return UNIX;
                default:
                    if (arg.charAt(0) != '-') return UNKNOWN;
                    if (arg.charAt(1) == '-') return GNU;
                    return UNIX;
            }
        }

        static String calcTimeFilename() {
            return DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())
                    .replace(':', '.').concat(".file");
        }
    }

    @Override
    public void launch(String... args) {

    }
}
