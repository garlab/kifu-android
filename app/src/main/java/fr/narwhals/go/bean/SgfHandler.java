package fr.narwhals.go.bean;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.io.IOException;

import fr.narwhals.go.domain.Go;
import fr.narwhals.go.sgf.SgfComposer;
import fr.narwhals.go.util.FileUtil;

@EBean
public class SgfHandler {
    static final String SGF_DIR = "sgf";

    @RootContext Context context;
    String fileName;

    @AfterInject
    void initFileName() {
        this.fileName = "kifu_" + System.currentTimeMillis() + ".sgf";
    }

    public File save(Go game) throws IOException {
        File sgfFile = getSgfFile();
        String sgf = new SgfComposer(game).toString();
        FileUtil.save(sgfFile, sgf);
        indexFile(sgfFile);

        return sgfFile;
    }

    private void indexFile(File file) {
        // TODO: Find a workaround for modified files, since this will only work for newly created files
        // Maybe remove the file first, using getContentResolver().delete(uri, null, null)
        Intent scanFile = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)
        );
        context.sendBroadcast(scanFile);
    }

    private File getSgfFile() throws IOException {
        if (!FileUtil.isExternalStorageWritable()) {
            throw new IOException("External storage is not writable");
        }

        File sgfDir = getSgfStorageDirectory();
        if (!sgfDir.mkdirs() && !sgfDir.isDirectory()) {
            throw new IOException("Can't create the sgf directory");
        }

        return new File(sgfDir, fileName);
    }

    public static File getSgfStorageDirectory() {
        return new File(FileUtil.getDocumentsDirectory(), "sgf");
    }
}
