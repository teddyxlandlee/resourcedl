package xland.ioutils.resourcedl.console.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xland.ioutils.resourcedl.console.ChecksumProcessor;
import xland.ioutils.resourcedl.download.HashedDownload;
import xland.ioutils.resourcedl.download.UriHashRule;
import xland.ioutils.resourcedl.hashing.Hasher;
import xland.ioutils.resourcedl.util.IOUtils;
import xland.ioutils.resourcedl.util.spi.MultiFileDownloadProvider;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static xland.ioutils.resourcedl.console.main.ArgParser.currentTime;
import static xland.ioutils.resourcedl.console.main.ArgParser.nse;

interface RunnableGetters {

    static IOUtils.IORunnable download(List<Arg> args1) {
        if (HelpMessages.containsHelpArg(args1))
            return HelpMessages.log(HelpMessages::download);

        URI root = null;
        String hash = null;
        UriHashRule rule = UriHashRule.sha1(true, false);//R2
        boolean print = false;
        boolean defOutput = false;
        Path output = Paths.get(currentTime());

        ListIterator<Arg> iterator = args1.listIterator();
        while (iterator.hasNext()) {
            Arg arg = iterator.next();
            if (arg.isGnu()) {
                switch (arg.getValue()) {
                    case "root":
                        arg = iterator.next();  // NEE
                        if (!arg.isCommon()) throw nse("root");
                        root = URI.create(arg.getValue());
                        break;
                    case "hash":
                        arg = iterator.next();
                        if (!arg.isCommon()) throw nse("hash");
                        hash = arg.getValue();
                        break;
                    case "rule":
                        arg = iterator.next();
                        if (!arg.isCommon()) throw nse("rule");
                        rule = UriHashRule.fromDesc(arg.getValue());
                        break;
                    case "output":
                        arg = iterator.next();
                        if (!arg.isCommon()) throw nse("output");
                        defOutput = true;
                        output = Paths.get(arg.getValue());
                        break;
                    case "print":
                        print = true;
                }
            }
        }
        print &= !defOutput;
        HashedDownload hd = new HashedDownload(root, output, hash, rule);
        if (!print) return hd;
        return new URLPrinter(hd);
    }

    static IOUtils.IORunnable checksum(List<Arg> args) /*throws IOException*/ {
        if (HelpMessages.containsHelpArg(args))
            return HelpMessages.log(HelpMessages::checksum);

        Path root = null;
        boolean defRoot = false;
        boolean print = false;
        UriHashRule rule = UriHashRule.sha1(true, false);//R2
        Hasher hasher = Hasher.sha256();
        //Path output = null;
        boolean interactive = false;
        final List<Path> originalFiles = new ArrayList<>();

        ListIterator<Arg> iterator = args.listIterator();
        while (iterator.hasNext()) {
            Arg arg = iterator.next();
            if (arg.isCommon()) originalFiles.add(Paths.get(arg.getValue()));
            else if (arg.isGnu()) {
                switch (arg.getValue()) {
                    case "root":
                        arg = iterator.next();  // NEE
                        if (!arg.isCommon()) throw nse("root");
                        defRoot = true;
                        root = Paths.get(arg.getValue());
                        break;
                    case "print":
                        print = true;
                        break;
                    case "rule":
                        arg = iterator.next();  // NEE
                        if (!arg.isCommon()) throw nse("rule");
                        rule = UriHashRule.fromDesc(arg.getValue());
                        break;
                    case "hasher":
                        arg = iterator.next();  // NEE
                        if (!arg.isCommon()) throw nse("hasher");
                        hasher = Hasher.getHasher(arg.getValue());
                        break;
                    //case "output":
                    //    arg = iterator.next();   // NEE
                    //    if (!arg.isCommon()) throw nse("output");
                    //    output = Paths.get(arg.getValue());
                    case "interactive":
                        interactive = true;
                        break;
                }
            }
        }
        print &= !defRoot;
        if (print)
            return new ChecksumPrinter(rule, originalFiles, hasher);


        final Path finalRoot = Objects.requireNonNull(root, "root");
        final Hasher finalHasher = Objects.requireNonNull(hasher, "hasher");
        final UriHashRule finalRule = Objects.requireNonNull(rule, "rule");
        final boolean finalInteractive = interactive;
        List<ChecksumProcessor> processors = originalFiles.stream() // TODO parallel?
                .map(p -> new ChecksumProcessor(finalRoot, finalHasher, finalRule, p,
                        finalInteractive))
                .collect(Collectors.toList());
        return new MultiTaskProcessors(processors);
    }

    static IOUtils.IORunnable multiFile(List<Arg> args) throws IOException {
        if (HelpMessages.containsHelpArg(args)) {
            return HelpMessages.log(HelpMessages::multiFile);
        }

        Iterator<MultiFileDownloadProvider> providers = ServiceLoader.load(MultiFileDownloadProvider.class).iterator();
        if (!providers.hasNext())
            ConsoleApp.LOGGER.error("`--multifile` is unsupported: no service provided");
        MultiFileDownloadProvider provider = providers.next();
        if (providers.hasNext())
            ConsoleApp.LOGGER.warn("More than one multifile download provider are provided");

        Path output = null;
        List<Path> json = new ArrayList<>();

        ListIterator<Arg> iterator = args.listIterator();
        while (iterator.hasNext()) {
            Arg arg = iterator.next();
            if (arg.isGnu()) {
                if ("output".equals(arg.getValue())) {
                    arg = iterator.next();  // NEE
                    if (!arg.isCommon()) throw nse("output");
                    output = Paths.get(arg.getValue());
                }
            } else if (arg.isCommon()) {
                json.add(Paths.get(arg.getValue()));
            }
        }

        if (json.isEmpty()) {
            ConsoleApp.LOGGER.warn("No files provided");
            return () -> {};
        } else {
            List<IOUtils.IORunnable> l = new ArrayList<>();
            for (Path p : json) l.addAll(provider.getDownloadEngine(p, output));
            return new MultiTaskProcessors(l);
        }
    }

    final class URLPrinter implements IOUtils.IORunnable {
        private static final Logger LOGGER = LoggerFactory.getLogger("URLPrinter");
        private final HashedDownload hd;

        public URLPrinter(HashedDownload hd) {
            this.hd = hd;
        }

        @Override
        public void runIo() throws IOException {
            LOGGER.info("URL: {}", hd.getUrl());
        }
    }

    final class MultiTaskProcessors implements IOUtils.IORunnable {
        private final List<? extends IOUtils.IORunnable> processors;

        public MultiTaskProcessors(List<? extends IOUtils.IORunnable> processors) {
            this.processors = processors;
        }

        @Override
        public void runIo() throws IOException {
            for (IOUtils.IORunnable r : processors)
                r.runIo();
        }

        @Override
        public String toString() {
            return String.format("MultiTaskProcessors[%s]", processors);
        }
    }

    final class ChecksumPrinter implements IOUtils.IORunnable {
        private final UriHashRule rule;
        private final List<Path> paths;
        private final Hasher hasher;

        private static final Logger LOGGER = LoggerFactory.getLogger("ChecksumPrinter");
        private static final Path nulPath = Paths.get("");

        ChecksumPrinter(UriHashRule rule, List<Path> paths, Hasher hasher) {
            this.rule = rule;
            this.paths = paths;
            this.hasher = hasher;
        }

        @Override
        public void runIo() throws IOException {
            StringBuilder sb = new StringBuilder("Hashing results:");
            for (Path path : paths) {
                String hash = hasher.hash(Files.newInputStream(path)).toString();
                Path resolved = rule.getFilePath(nulPath, hash);
                sb.append("\n - ").append(path).append("\t=> ").append(resolved);
            }
            LOGGER.info(sb.toString());
        }
    }
}
