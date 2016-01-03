package fr.narwhals.go.bean;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.LinearLayout;

import org.androidannotations.annotations.*;

import java.io.File;
import java.io.IOException;

import fr.narwhals.go.domain.Game;
import fr.narwhals.go.sgf.SgfComposer;
import fr.narwhals.go.util.FileUtil;

@EBean
public class SgfHandler implements View.OnClickListener {

    protected @RootContext Context context;
    protected @ViewById LinearLayout gameLayout;

    private Game game;
    private String fileName;
    private String fileContent;

    @AfterInject
    public void initFileName() {
        this.fileName = "kifu_" + System.currentTimeMillis() + ".sgf";
    }

    public void save(Game game) {
        this.game = game;
        this.fileContent = null;

        save();
    }

    private void save() {
        try {
            File file = getSgfFile();
            String sgf = getFileContent();
            FileUtil.save(file, sgf);
            indexFile(file);

            Snackbar.make(gameLayout, "Game saved", Snackbar.LENGTH_SHORT)
                    .show();
        } catch (IOException e) {
            Snackbar.make(gameLayout, e.getMessage(), Snackbar.LENGTH_LONG)
                    .setAction("RETRY", this)
                    .show();
        }
    }

    @Override
    public void onClick(View v) {
        save();
    }

    private String getFileContent() {
        if (fileContent == null) {
            fileContent = new SgfComposer().compose(game).toString();
        }
        return fileContent;
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
