package xland.test.ioutils.resourcedl.console;

import xland.ioutils.resourcedl.console.main.ConsoleApp;

public class ConsoleUrlShowInternalTest {
    static final ConsoleApp consoleApp = new ConsoleApp();

    //@Test
    public static void main(String[] args) {
        consoleApp.launch(
                "-Dr", "https://featurehouse.github.io/resources",
                "-s", "eb5a6d8856072890b61f9bd185014ce921a862c3aa831908dc9b8976e2879c16",
                "-u", "2,4,6",
                "-o", "/tmp/Contract2020-1.pdf");
    }
}
