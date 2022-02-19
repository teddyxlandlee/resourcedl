package xland.ioutils.resourcedl.hashing;

import xland.ioutils.resourcedl.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Hasher {
    private final MessageDigest messageDigest;

    public Hasher(MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
    }

    private static final Map<String, Supplier<Hasher>> hasherSupplierMap;
    static {
        Map<String, Supplier<Hasher>> m = new HashMap<>();
        m.put("sha256", Hasher::sha256);
        m.put("sha1", Hasher::sha1);
        m.put("sha512", Hasher::sha512);
        m.put("sha224", Hasher::sha224);
        m.put("sha384", Hasher::sha384);
        m.put("md5", Hasher::md5);
        m.put("md2", Hasher::md2);

        hasherSupplierMap = Collections.unmodifiableMap(m);
    }

    public static Hasher getHasher(String name)
                throws IllegalArgumentException {
        Supplier<Hasher> supplier = hasherSupplierMap.get(name);
        if (supplier == null) {
            throw new IllegalArgumentException(name);
        } else return supplier.get();
    }

    private static Hasher getHasherInternal(String messageDigest) {
        try {
            return new Hasher(MessageDigest.getInstance(messageDigest));
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    public static Hasher sha256() {
        return getHasherInternal("SHA-256");
    }

    public static Hasher sha1() {
        return getHasherInternal("SHA-1");
    }

    public static Hasher sha512() {
        return getHasherInternal("SHA-512");
    }

    public static Hasher sha224() {
        return getHasherInternal("SHA-224");
    }

    public static Hasher sha384() {
        return getHasherInternal("SHA-384");
    }

    public static Hasher md5() {
        return getHasherInternal("MD5");
    }

    public static Hasher md2() {
        return getHasherInternal("MD2");
    }

    public HashingResult hash(InputStream inputStream) throws IOException {
        BufferedInputStream is = IOUtils.buffered(inputStream);
        byte[] buffer = new byte[4096];
        int len;
        while ((len = is.read(buffer, 0, 4096)) > 0) {
            messageDigest.update(buffer, 0, len);
        }
        return new HashingResult(messageDigest.digest());
    }

    public HashingResult hash(byte[] b) {
        return new HashingResult(messageDigest.digest(b));
    }

    public HashingResult hash(String s) {
        return hash(s.getBytes(StandardCharsets.UTF_8));
    }

    public HashingResult hash(String s, Charset charset) {
        return hash(s.getBytes(charset));
    }

    public HashingResult hash(String s, String charsetName) throws UnsupportedEncodingException {
        return hash(s.getBytes(charsetName));
    }
}
