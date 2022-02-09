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

public class Hasher {
    private final MessageDigest messageDigest;

    public Hasher(MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
    }

    public static Hasher getHasher(String messageDigest) {
        try {
            return new Hasher(MessageDigest.getInstance(messageDigest));
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError(e);
        }
    }

    public static Hasher sha256() {
        return getHasher("SHA-256");
    }

    public static Hasher sha1() {
        return getHasher("SHA-1");
    }

    public static Hasher sha512() {
        return getHasher("SHA-512");
    }

    public static Hasher sha224() {
        return getHasher("SHA-224");
    }

    public static Hasher sha384() {
        return getHasher("SHA-384");
    }

    public static Hasher md5() {
        return getHasher("MD5");
    }

    public static Hasher md2() {
        return getHasher("MD2");
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
