package Util;

import java.io.File;
import java.net.URL;

public class FileUtils {
    public static File getFile(String fileName) {

        ClassLoader classLoader = FileUtils.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return new File(resource.getFile());
        }
    }
}
