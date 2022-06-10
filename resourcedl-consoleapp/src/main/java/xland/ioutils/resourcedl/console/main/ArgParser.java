package xland.ioutils.resourcedl.console.main;

import xland.ioutils.resourcedl.util.IOUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

final class ArgParser {
    interface CommonMaps {
        Map<String, String> MAJOR = PropertyBuilder.<String, String>of()
                .add("h", "help")
                .add("D", "download")
                .add("H", "checksum")
                .add("M", "multifile")
                .build();
        Map<String, Map<String, String>> MINOR = PropertyBuilder.<String, Map<String, String>>of()
                .add("download", PropertyBuilder.<String, String>of()
                        .add("h", "help")
                        .add("r", "root")
                        .add("s", "hash")
                        .add("u", "rule")
                        .add("o", "output")
                        .add("p", "print")
                        .build()
                ).add("checksum", PropertyBuilder.<String, String>of()
                        .add("h", "help")
                        .add("r", "root")
                        .add("p", "print")
                        .add("u", "rule")
                        .add("a", "hasher")
                        .add("o", "output")
                        .add("i", "interactive")

                        .add("o", "output")
                        .add("O", "stdout")
                        .add("R", "relative")
                        .add("e", "target")

                        .add("L", "print-url")
                        .build()
                ).add("multifile", PropertyBuilder.<String, String>of()
                        .add("o", "output")
                        .add("h", "help")
                        .build()
                ).add("help", Collections.emptyMap()).build();
        Map<String, IOUtils.IOFunction<List<Arg>, IOUtils.IORunnable>> RUNNABLE
                = PropertyBuilder.<String, IOUtils.IOFunction<List<Arg>, IOUtils.IORunnable>>of()
                .add("help", HelpMessages::root)
                .add("download", RunnableGetters::download)
                .add("checksum", RunnableGetters::checksum)
                .add("multifile", RunnableGetters::multiFile)
                .build();
    }

    final String[] args;
    transient final List<Arg> parsedList;

    ArgParser(String[] args) {
        this.args = args;

        parsedList = Arrays.stream(args)
                .flatMap(Arg::fromString)
                .map(Arg::mapToGnu)
                .collect(Collectors.toList());
    }

    IOUtils.IORunnable parse() throws NoSuchElementException, IOException {
        if (parsedList.isEmpty()) throw nse("required arguments");

        Optional<Arg> first = parsedList.stream().filter(Arg::isGnu).findFirst();
        if (!first.isPresent()) {
            throw nse("required major process");
        } else {
            String arg = first.get().getValue();
            final Map<String, String> minor = CommonMaps.MINOR.get(arg);
            if (minor == null) throw nse("Illegal major process: " + arg);
            List<Arg> argList = parsedList.stream()
                    .map(a -> Arg.mapToGnu(a, minor))
                    .collect(Collectors.toList());
            return CommonMaps.RUNNABLE.get(arg)
                    .applyIo(argList);
        }
    }

    static String currentTime() {
        return DateTimeFormatter.ISO_DATE_TIME
                .format(LocalDateTime.now())
                .replace(':', '.').concat(".file");
    }

    static NoSuchElementException nse(String s) { return new NoSuchElementException(s); }
}
