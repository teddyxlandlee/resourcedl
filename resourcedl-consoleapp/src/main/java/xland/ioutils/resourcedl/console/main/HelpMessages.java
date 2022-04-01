package xland.ioutils.resourcedl.console.main;

import xland.ioutils.resourcedl.util.IOUtils;

import java.util.List;
import java.util.function.Supplier;

interface HelpMessages {
    static String download() {
        return "-D / --download:\n" +
                "\t-r / --root <url>: the root URL of the resource website\n" +
                "\t-s / --hash <hash>\n" +
                "\t-u / --rule <hashrule>: hash string splitting rule. Type `-h rule` to" +
                    " get further information.\n" +
                "\t-o / --output [filename], default <current-time>.file\n" +
                "\t-p / --print: if --output doesn't exist, just print the URL to stdout";
    }

    static String checksum() {
        return "-H / --checksum:\n" +
                "\t-r / --root <path>: the root directory to be copied in\n" +
                "\t-p / --print: if --root doesn't exist, just print the abstract path to stdout\n" +
                "\t-u / --rule <hashrule>: hash string splitting rule. Type `-h rule` to" +
                    " get further information.\n" +
                "\t-a / --hasher <hasher>: sha256, sha1, ... (default sha256)\n" +
                "\t-o / --output <file>: if possible, print multi-file configuration to the file\n" +
                "\t-O / --stdout: if possible, print multi-file configuration to stdout\n" +
                "\t-R / --relative <root>: when generating multi-file configuration, use relative path\n" +
                "\t-e / --target <uri>: when generating multi-file configuration, use target URI\n" +
                "\tpaths to the original files";
    }

    static String hashRule() {
        return "Hash rule is something like this:\n" +
                "- RS2,4,6\t// Repeats, HasSubDirectory, CutIndexes=2,4,6\n" +
                "- S3\t// Doesn't repeat, HasSubDirectory, CutIndexes=3\n" +
                "\n" +
                "For example, we are processing a file called `one.txt`, hashes 0123456789abcdef.\n" +
                "- R2,4,6\t-> 01/23/45/0123456789abcdef\n" +
                "- 2,4,6\t-> 01/23/45/6789abcdef\n" +
                "- S2,4,6\t-> 01/23/45/6789abcdef/file along with 01/23/45/6789abcdef/one.txt\n" +
                "\n" +
                "Default: R2\t//Repeats, CutIndexes=2, which is Minecraft-style" +
                    " used in https://resources.download.minecraft.net/.";
    }

    static String multiFile() {
        return "-M / --multifile:\n" +
                "\t-o / --output: destination directory\n" +
                "\tpaths to the original json file";
        //TODO enable parameter URI/URL
    }

    static String all() {
        return "Arguments:\n\n" + download() + "\n\n" + checksum()
                + "\n\n" + multiFile();
    }

    static boolean containsHelpArg(List<Arg> args) {
        return args.contains(Arg.gnu("help"));
    }

    static IOUtils.IORunnable log(Supplier<String> supplier) {
        return () -> ConsoleApp.LOGGER.info(supplier.get());
    }

    static IOUtils.IORunnable root(List<Arg> args) {
        if (args.contains(Arg.common("rule"))) return log(HelpMessages::hashRule);
        return log(HelpMessages::all);
    }
}
