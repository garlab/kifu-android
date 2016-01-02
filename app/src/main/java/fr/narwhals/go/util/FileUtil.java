package fr.narwhals.go.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    static final String LOG_TAG = "FileUtil";
    static final String SGF_DIR = "sgf";

    public static Intent indexFile(File file) {
        // TODO: Find a workaround for modified files, since this will only work for newly created files
        // Maybe remove the file first, using getContentResolver().delete(uri, null, null)
        return new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)
        );
    }

    public static File saveSgf(String fileName, String content) {
        if (!isExternalStorageWritable()) {
            Log.w(LOG_TAG, "External storage is not writable");
            return null;
        }

        File sgfFile = getSgfStorageFile(fileName);
        if (sgfFile == null) {
            Log.w(LOG_TAG, "Can't create the sgf file");
            return null;
        }

        try {
            saveSgf(sgfFile, content);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Can't write in the sgf file", e);
            return null;
        }

        return sgfFile;
    }

    private static void saveSgf(File sgfFile, String content) throws IOException {
        byte[] bytes = content.getBytes();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(sgfFile);
            fos.write(bytes);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static File getSgfStorageDirectory() {
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), SGF_DIR);
    }

    public static File getSgfStorageFile(String fileName) {
        File sgfDir = getSgfStorageDirectory();
        if (!sgfDir.mkdirs() && !sgfDir.isDirectory()) {
            return null;
        }

        return new File(sgfDir, fileName);
    }

    private static int readInt(InputStream in) throws IOException {
        int ret = 0;
        boolean dig = false;

        for (int c = 0; (c = in.read()) != -1; ) {
            if (c >= '0' && c <= '9') {
                dig = true;
                ret = ret * 10 + c - '0';
            } else if (dig) break;
        }

        return ret;
    }

    private static char[] readCharArray(InputStream in, int n) throws IOException {
        char[] arr = new char[n];

        for (int c = 0; (c = in.read()) != -1; ) {
            if (Character.isLetterOrDigit(c)) {
                arr[0] = (char)c;
                break;
            }
        }

        for (int i = 1; i < n; ++i) {
            arr[i] = (char)in.read();
        }

        return arr;
    }
}
