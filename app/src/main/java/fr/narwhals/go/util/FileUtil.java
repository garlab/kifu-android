package fr.narwhals.go.util;

import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

    public static File getDocumentsDirectory() {
        return Build.VERSION.SDK_INT >= 19 ?
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) :
                new File(Environment.getExternalStorageDirectory() + "/Documents");
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

    public static void save(File file, String content) throws IOException {
        byte[] bytes = content.getBytes();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bytes);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
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
