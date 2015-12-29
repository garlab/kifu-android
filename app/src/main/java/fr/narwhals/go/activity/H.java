package fr.narwhals.go.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public final class H {
    public static void showError(Throwable t, Context context) {
        Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        Log.e(context.getClass().getName(), t.getLocalizedMessage(), t);
    }

    public static Bitmap getBitmap(int drawable, int width, int height, Resources res) {
        Bitmap bm = BitmapFactory.decodeResource(res, drawable);

        Matrix matrix = new Matrix();
        matrix.postScale((float) width / (float) bm.getWidth(), (float) height / (float) bm.getHeight());

        Bitmap resized = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false);
        bm.recycle();

        return resized;
    }

    public static void serialize(Serializable object, String filename, Context context) throws IOException {
        OutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE);
        OutputStream buffer = new BufferedOutputStream(file);
        ObjectOutput output = new ObjectOutputStream(buffer);
        try {
            output.writeObject(object);
        } finally {
            output.close();
        }
    }

    public static Object unserialize(String filename, Context context) throws IOException, ClassNotFoundException {
        File file = context.getFileStreamPath(filename);
        InputStream stream = new FileInputStream(file);
        InputStream buffer = new BufferedInputStream(stream);
        ObjectInput input = new ObjectInputStream(buffer);
        Object object = null;
        try {
            object = input.readObject();
            return object;
        } finally {
            input.close();
            if (object == null) { /* archive file is corupted */
                context.deleteFile(filename);
            }
        }
    }

    public static void pause(View view) throws InterruptedException {
        view.invalidate();
        view.setClickable(false);
        Thread.currentThread();
        try {
            Thread.sleep(200);
        } finally {
            view.setClickable(true);
        }
    }

    public static Intent newGame(Context packageContext) {
        return new Intent(packageContext, NewGameActivity_.class);
    }
}
