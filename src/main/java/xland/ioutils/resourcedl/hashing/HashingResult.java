package xland.ioutils.resourcedl.hashing;

import java.util.Locale;

public class HashingResult {
    private final byte[] hash;

    public HashingResult(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getBytes() {
        return hash.clone();
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        byte[] hash = this.hash;
        for (int b : hash) {
            if (b < 0) b += 256;
            sb.append(hexChars[b >> 4]);
            sb.append(hexChars[b & 15]);
        }
        return sb.toString();
    }

    public String toString(boolean uppercase) {
        String lowercase = toString();
        if (uppercase) return lowercase.toUpperCase(Locale.ROOT);
        return lowercase;
    }

    private static final char[] hexChars = "0123456789abcdef".toCharArray();
}
