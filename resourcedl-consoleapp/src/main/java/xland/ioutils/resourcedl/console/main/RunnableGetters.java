package xland.ioutils.resourcedl.console.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xland.ioutils.resourcedl.console.ChecksumProcessor;
import xland.ioutils.resourcedl.download.HashedDownload;
import xland.ioutils.resourcedl.download.UriHashRule;
import xland.ioutils.resourcedl.hashing.Hasher;
import xland.ioutils.resourcedl.util.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
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
        Path output = null;
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
                    case "output":
                        arg = iterator.next();   // NEE
                        if (!arg.isCommon()) throw nse("output");
                        output = Paths.get(arg.getValue());
                }
            }
        }
        print &= !defRoot;
        if (print)
            return new ChecksumPrinter(rule, originalFiles, hasher);


        final Path finalRoot = Objects.requireNonNull(root, "root");
        final Hasher finalHasher = Objects.requireNonNull(hasher, "hasher");
        final UriHashRule finalRule = Objects.requireNonNull(rule, "rule");
        List<ChecksumProcessor> processors = originalFiles.stream() // TODO parallel?
                .map(p -> new ChecksumProcessor(finalRoot, finalHasher, finalRule, p))
                .collect(Collectors.toList());
        return new MultiChecksumProcessor(processors);
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

    final class MultiChecksumProcessor implements IOUtils.IORunnable {
        //TODO: MultiThread support
        private final List<ChecksumProcessor> processors;

        public MultiChecksumProcessor(List<ChecksumProcessor> processors) {
            this.processors = processors;
        }

        @Override
        public void runIo() throws IOException {
            for (ChecksumProcessor cp : processors)
                cp.runIo();
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
