package xland.ioutils.resourcedl;

import xland.ioutils.resourcedl.util.spi.ConsoleRDAppProvider;
import xland.ioutils.resourcedl.util.spi.GuiRDAppProvider;

import java.util.Iterator;
import java.util.ServiceLoader;

public class Main {
    public static void main(String... args) {
        if (args.length == 0) {
            Iterator<GuiRDAppProvider> itr = ServiceLoader.load(GuiRDAppProvider.class).iterator();
            if (!itr.hasNext()) {
                System.err.println("Error: Gui App is not supported.\n" +
                        "Try type `--help` for help.");
                System.exit(-1);
            } else {
                GuiRDAppProvider provider = itr.next();
                if (itr.hasNext()) {
                    System.err.println("Warning: More than one gui app are registered." +
                            " Using the default one.");
                }
                provider.run();
            }
        }
        else {
            Iterator<ConsoleRDAppProvider> itr = ServiceLoader.load(ConsoleRDAppProvider.class).iterator();
            if (!itr.hasNext()) {
                System.err.println("Error: Console App is not supported");
                System.exit(-1);
            } else {
                ConsoleRDAppProvider provider = itr.next();
                if (itr.hasNext()) {
                    System.err.println("Warning: More than one console app are registered." +
                            " Using the default one.");
                }
                provider.launch(args);
            }
        }
    }
}
