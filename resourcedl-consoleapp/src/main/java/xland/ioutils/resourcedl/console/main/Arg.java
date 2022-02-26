package xland.ioutils.resourcedl.console.main;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class Arg {
    public static final int GNU = 0, UNIX = 1, COMMON = -1;

    private final int type;
    private final String value;

    Arg(int type, String value) {
        Objects.requireNonNull(value, "value");
        this.type = type;
        this.value = value;
    }

    static Arg gnu(String value) { return new Arg(GNU, value); }
    static Arg unix(String value) { return new Arg(UNIX, value); }
    static Arg common(String value) { return new Arg(COMMON, value); }

    /**
     * @param oneArg one argument from {@code runArgs}, e.g.
     *               {@code -Hpa}, {@code --download}, {@code https://www.example.com/}.
     */
    static Stream<Arg> fromString(String oneArg) {
        switch (oneArg.length()) {
            case 0:
                return Stream.empty();
            case 1:
                return common1(oneArg);
            case 2:
                if (oneArg.charAt(0) != '-') return common1(oneArg);
                return Stream.of(unix(oneArg.substring(1)));
            default:
                if (oneArg.charAt(0) != '-') return common1(oneArg);
                if (oneArg.charAt(1) == '-') return Stream.of(gnu(oneArg.substring(2)));
                // this is unix.
                return oneArg.chars().skip(1)
                        .mapToObj(i -> unix(String.valueOf((char)i)));
        }
    }

    public boolean isUnix() { return type == UNIX; }
    public boolean isGnu() { return type == GNU; }
    public boolean isCommon() { return type == COMMON; }

    static Arg mapToGnu(Arg arg) {
        return mapToGnu(arg, ArgParser.CommonMaps.MAJOR);
    }

    static Arg mapToGnu(Arg arg, Map<String, String> map) {
        if (!arg.isUnix()) return arg;
        String mapped = map.get(arg.value);
        if (mapped == null) return common(arg.value);
        return gnu(mapped);
    }

    private static Stream<Arg> common1(String s) { return Stream.of(common(s)); }

    @Override
    public String toString() {
        return "Arg{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arg arg = (Arg) o;
        return type == arg.type && value.equals(arg.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    public int getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
