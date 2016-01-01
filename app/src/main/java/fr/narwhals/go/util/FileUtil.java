package fr.narwhals.go.util;

import android.os.Environment;

import java.io.File;

public class FileUtil {
    public static final String SGF_DIR = "sgf";

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static File getSgfStorageDir() {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), SGF_DIR);
        if (!file.mkdirs() && !file.isDirectory()) {
            return null;
        }
        return file;
    }

    public static boolean saveSgf(String fileName, String content) {
        File sgfDir = getSgfStorageDir();
        if (sgfDir == null) {
            return false;
        }
        return false;
    }
}
