package xland.ioutils.resourcedl.download;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;

public class UriHashRule {
    //private final int length;
    private final int[] cutIndexes;
    private final boolean repeat;
    private final boolean hasSubDirectory;

    public UriHashRule(int[] cutIndexes, boolean repeat, boolean hasSubDirectory) {
        Objects.requireNonNull(cutIndexes, "cutIndexes");
        //this.length = length;
        this.cutIndexes = Arrays.stream(cutIndexes).sorted().toArray();
        this.repeat = repeat;
        this.hasSubDirectory = hasSubDirectory;
    }

    private UriHashRule(int[] cutIndexes, boolean repeat, boolean hasSubDirectory,
                        @SuppressWarnings("unused") Void _marker) {
        this.cutIndexes = cutIndexes;
        this.repeat = repeat;
        this.hasSubDirectory = hasSubDirectory;
    }

    private static final int[] sha256c246 = {2, 4, 6};
    private static final int[] sha256c24x = {2, 4, 10};
    private static final int[] sha1 = {2};

    public static UriHashRule sha256c246(boolean repeat, boolean hasSubDirectory) {
        return new UriHashRule(sha256c246, repeat, hasSubDirectory, null);
    }

    public static UriHashRule sha1(boolean repeat, boolean hasSubDirectory) {
        return new UriHashRule(sha1, repeat, hasSubDirectory, null);
    }

    public static UriHashRule sha256c24x(boolean repeat, boolean hasSubDirectory) {
        return new UriHashRule(sha256c24x, repeat, hasSubDirectory, null);
    }

    /**
     * @throws IndexOutOfBoundsException if any of cut indexes is out
     * of {@code hash.length()}.
     * @throws IllegalArgumentException if the hash violates URI syntax.
     */
    public URI getFileUri(URI root, final String hash) {
        String protocol = root.getScheme();
        if (protocol == null) protocol = "http";   // default
        if (root.isOpaque()) {
            String ssp = catSsp(root.getSchemeSpecificPart(), hash);
            try {
                return new URI(protocol, ssp, null);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid ssp: " + ssp, e);
            }
        } else {
            String path = catSsp(root.getPath(), hash);
            try {
                return new URI(protocol, root.getUserInfo(), root.getHost(),
                        root.getPort(), path, null, null);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    private String catSsp(String ssp, final String hash) {
        int len0 = this.cutIndexes.length;
        StringBuilder sb = new StringBuilder(ssp);
        if (ssp.endsWith("/")) { sb.deleteCharAt(ssp.length() - 1); }
        if (len0 == 0) {
            return sb.append('/').append(hash).toString();
        }

        int lastIndex = 0;
        for (int index : this.cutIndexes) {
            sb.append('/').append(hash, lastIndex, index);
            lastIndex = index;
        }
        if (this.repeat) {
            sb.append('/').append(hash);
        } else {
            sb.append('/').append(hash, lastIndex, hash.length());
        }
        if (this.hasSubDirectory) {
            sb.append("/file");
        }
        return sb.toString();
    }

    /**
     * @param desc is something like this:
     * <p>{@code RS2,4,6}   // Repeats, HasSubDirectory, CutIndexes=2,4,6</p>
     * <p>{@code S3}        // Doesn't repeat, HasSubDirectory, CutIndexes=3</p>
     */
    public static UriHashRule fromDesc(final String desc) throws NumberFormatException {
        int len0 = desc.length();
        boolean repeat = false, hasSubDirectory = false;
        int[] val = new int[0];
        for (int i = 0; i < len0; i++) {
            char c0 = desc.charAt(i);
            if (Character.isAlphabetic(c0)) {
                if (c0 == 'S' || c0 == 's') hasSubDirectory = true;
                else if (c0 == 'R' || c0 == 'r') repeat = true;
                else throw new NumberFormatException("Illegal desc at pointer " +
                            i + ", char '" + c0 + '\'');
            } else if (Character.isDigit(c0)) {
                val = Arrays.stream(desc.substring(i, len0).split(","))
                        .mapToInt(Integer::parseUnsignedInt).toArray();
                break;
            } else throw new NumberFormatException("Illegal desc at pointer " +
                    i + ", char '" + c0 + '\'');
        }
        return new UriHashRule(val, repeat, hasSubDirectory);
    }

    public int[] cutIndexes() {
        return cutIndexes.clone();
    }

    public boolean repeat() {
        return repeat;
    }

    public boolean hasSubDirectory() {
        return hasSubDirectory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UriHashRule that = (UriHashRule) o;
        return  repeat == that.repeat &&
                hasSubDirectory == that.hasSubDirectory
                && Arrays.equals(cutIndexes, that.cutIndexes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(repeat, hasSubDirectory);
        result = 31 * result + Arrays.hashCode(cutIndexes);
        return result;
    }

    @Override
    public String toString() {
        return "UriHashRule{" +
                "cutIndexes=" + Arrays.toString(cutIndexes) +
                ", repeat=" + repeat +
                ", hasSubDirectory=" + hasSubDirectory +
                '}';
    }
}
