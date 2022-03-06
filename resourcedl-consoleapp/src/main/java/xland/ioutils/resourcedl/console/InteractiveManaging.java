package xland.ioutils.resourcedl.console;

import java.io.IOException;

public final class InteractiveManaging {
    public static Boolean readYesOrNo(String tip) throws IOException {
        System.out.print(tip);
        switch (System.in.read()) {
            case 'Y':
            case 'y':
                return Boolean.TRUE;
            case 'n':
            case 'N':
                return Boolean.FALSE;
        }
        return null;
    }

    public static boolean readYesOrNo(String tip, boolean defaultValue) throws IOException {
        Boolean readYesOrNo = readYesOrNo(tip);
        return readYesOrNo == null ? defaultValue : readYesOrNo;
    }
}
