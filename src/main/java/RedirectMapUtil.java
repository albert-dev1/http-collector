import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class RedirectMapUtil {

    public static final String PATH_301 = "site/301redirects.map";
    public static final String PATH_302 = "site/302redirects.map";

    public static void createFilesIfNotExists(){
        createFileIfNotExits(PATH_301);
        createFileIfNotExits(PATH_302);
    }

    public static void write301Redirect(String from, String to) {
        File yourFile = createFileIfNotExits(PATH_301);
        appendRedirect(from, to, PATH_301, yourFile);
    }

    public static void write302Redirect(String from, String to) {
        File yourFile = createFileIfNotExits(PATH_302);
        appendRedirect(from, to, PATH_302, yourFile);
    }

    private static void appendRedirect(String from, String to, String path301, File yourFile) {
        try(FileOutputStream oFile = new FileOutputStream(yourFile, true)) {
            Files.write(Paths.get(path301), String.format("%s %s;\r", from, to).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File createFileIfNotExits(String path301) {
        File yourFile = new File(path301);
        yourFile.getParentFile().mkdirs();
        try {
            yourFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return yourFile;
    }

    public static void writeRedirect(Redirect redirect) {
        switch (redirect.getType()) {
            case 301: write301Redirect(redirect.getSource(), redirect.getLocation()); break;
            case 302: write302Redirect(redirect.getSource(), redirect.getLocation()); break;
        }
    }
}
