package xland.test.ioutils.resourcedl.console;

import org.junit.jupiter.api.Test;
import xland.ioutils.resourcedl.console.ConsoleApp;

public class ConsoleUrlShowInternalTest {
    ConsoleApp consoleApp = new ConsoleApp();

    @Test
    public void test() {
        consoleApp.launch(
                "-Dr", "https://featurehouse.github.io/resources",
                "-s", "eb5a6d8856072890b61f9bd185014ce921a862c3aa831908dc9b8976e2879c16",
                "-u", "2,4,6",
                "-o", "F:\\.tmp\\Contract2020.pdf");
    }
}
