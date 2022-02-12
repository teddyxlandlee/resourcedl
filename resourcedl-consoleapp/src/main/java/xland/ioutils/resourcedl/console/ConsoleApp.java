package xland.ioutils.resourcedl.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xland.ioutils.resourcedl.console.download.DownloadWrapper;
import xland.ioutils.resourcedl.download.UriHashRule;
import xland.ioutils.resourcedl.util.IOUtils;
import xland.ioutils.resourcedl.util.spi.ConsoleRDAppProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * <h3>Arguments: </h3><ul>
 *   <!--<li>-h / --help</li>-->
 *   <li>-D / --download: <ul>
 *     <li>-r / --root &lt;url&gt;: the root URL of the resource website</li>
 *     <li>-s / --hash &lt;hash&gt;</li>
 *     <li>-u / --rule &lt;hashrule&gt;: see {@link UriHashRule#fromDesc(String)},
 *     default is {@link UriHashRule#sha1(boolean, boolean) R2} (Minecraft style)</li>
 *     <li>-o / --output [filename], default {@code &lt;current-time&gt;.file}</li>
 *     <li>-p / --print: if {@code --output} doesn't exist, just print the URL
 *     to stdout</li>
 *   </ul></li>
 *   <p>These above can be replaced with:</p>
 *   <li>&lt;path/to/properties&gt;: a properties file that contains all these above.
 *   For example: {@code <pre>
 * mode=download
 * root=https\://featurehouse.github.io/resources/
 * hash=dffd6021bb2bd5b0af676290809ec3a53191dd81c7f70a4b28688a362182986f
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

        private static final Properties defaultProperties = new Properties(); static {
            defaultProperties.setProperty("rule", "R2");
            //defaultProperties.setProperty("output", calcTimeFilename());
        }

        transient final Properties properties = new Properties(defaultProperties);
        final String[] args;

        static final Map<Character, String> unix2gnu; static {
            Map<Character, String> m = new Hashtable<>();
            m.put('h', "help");
            m.put('D', "download");
            m.put('r', "root");
            m.put('s', "hash");
            m.put('u', "rule");
            m.put('o', "output");
            m.put('p', "print");
            unix2gnu = Collections.unmodifiableMap(m);
        }
        public static final String DEFAULT_ACTIVITY = "download";

        static final Map<String, IOUtils.IOFunction<? super Properties, ? extends IOUtils.IORunnable>> ioRunnableMap;
        static {
            Map<String, IOUtils.IOFunction<? super Properties, ? extends IOUtils.IORunnable>> m = new HashMap<>();
            m.put("download", DownloadWrapper::get);

            ioRunnableMap = Collections.unmodifiableMap(m);
        }

        ArgumentParser(String... args) {
            this.args = args;
            readArgs();
        }

        void readArgs() {
            if (getArgType(args[0]) < 0) {
                // try parse it as path
                try {
                    Path path = Paths.get(args[0]);
                    if (Files.exists(path) && !Files.isDirectory(path)) {
                        properties.load(Files.newBufferedReader(path));
                    }
                } catch (IOException | InvalidPathException e) {
                    LOGGER.error("Failed to read property from {} because of {}", args[0], e);
                }
                return;
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
                        putToMap(lastArgument, null, i);
                        break;
                    case UNIX:
                        int argLen = arg.length();
                        for (int j = 1; j < argLen; j++) {
                            char c = arg.charAt(j);
                            if (unix2gnu.containsKey(c)) {
                                lastArgument = unix2gnu.get(c);
                                putToMap(lastArgument, null, i);
                            } else {
                                LOGGER.error("Illegal argument at index {}: invalid option `-{}`", i, c);
                                System.exit(-1);
                            }
                        }
                        break;
                }
            }

            /* HERE! If arguments are added, edit here */
            if (map.containsKey("download")) {
                properties.put("mode", "download");
            }
            properties.putAll(map);
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

        IOUtils.IORunnable parse() throws IOException {
            // default `download` when `mode` is not specified
            return ioRunnableMap.get(properties.getProperty("mode", DEFAULT_ACTIVITY))
                    .applyIo(properties);
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

    }

    @Override
    public void launch(String... args) {
        try {
            IOUtils.IORunnable runnable = new ConsoleApp.ArgumentParser(args).parse();
            LOGGER.info("Preparing to launch {}", runnable);
            runnable.runIo();
        } catch (Throwable t) {
            LOGGER.error("An unexpected error has occurred", t);
        }
    }
}
