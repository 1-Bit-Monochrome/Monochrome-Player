package com.monochrome.monochrome_player;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public final class BundledModelInstaller {
    private static final String TAG = "BundledModelInstaller";
    private static final String ASSET_MODELS_DIR = "models";

    private BundledModelInstaller() {
    }

    public static void installIfPresent(Context context, String modelFileName, String labelsFileName) {
        if (context == null) return;

        File modelsDir = new File(context.getFilesDir(), "models");
        if (!modelsDir.exists()) {
            boolean created = modelsDir.mkdirs();
            if (!created) return;
        }

        installSingleAssetIfPresent(context, modelFileName, new File(modelsDir, modelFileName));
        installSingleAssetIfPresent(context, labelsFileName, new File(modelsDir, labelsFileName));
    }

    private static void installSingleAssetIfPresent(Context context, String assetName, File targetFile) {
        if (assetName == null || assetName.trim().isEmpty()) return;
        if (targetFile.exists() && targetFile.length() > 0) return;

        String assetPath = ASSET_MODELS_DIR + "/" + assetName;
        AssetManager assetManager = context.getAssets();

        try (InputStream inputStream = assetManager.open(assetPath);
             FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            Log.i(TAG, "Installed bundled model asset: " + assetPath);
        } catch (Exception ignored) {
            Log.i(TAG, "Bundled asset not found, skipping: " + assetPath);
        }
    }
}
