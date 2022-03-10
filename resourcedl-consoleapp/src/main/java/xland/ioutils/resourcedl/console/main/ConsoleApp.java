package xland.ioutils.resourcedl.console.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xland.ioutils.resourcedl.download.UriHashRule;
import xland.ioutils.resourcedl.util.IOUtils;
import xland.ioutils.resourcedl.util.spi.ConsoleRDAppProvider;

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
 *   </ul></li><li>-H / --checksum: <ul>
 *       <li>-r / --root &lt;path&gt;: the root directory to be copied in</li>
 *       <li>-p / --print: if {@code --root} doesn't exist, just print the
 *       abstract path to stdout</li>
 *       <li>-u / --rule &lt;hashrule&gt;: see {@link UriHashRule#fromDesc(String)},
 *     default is {@link UriHashRule#sha1(boolean, boolean) R2} (Minecraft style)</li>
 *       <li>-a / --hasher &lt;hasher&gt;: sha256, sha1, ...</li>
 *       <li>paths to the original files</li>
 *   </ul></li>
 *
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

    @Override
    public void launch(String... args) {
        try {
            IOUtils.IORunnable runnable = //new ConsoleApp.ArgumentParser(args).parse();
                    new ArgParser(args).parse();
            LOGGER.info("Preparing to launch {}", runnable);
            runnable.runIo();
            LOGGER.info("Done");
        } catch (Throwable t) {
            LOGGER.error("An unexpected error has occurred", t);
        }
    }
}
