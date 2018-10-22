package edu.temple.eac.utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 */
public class DownloadConfigTask extends AsyncTask<String, Void, File> {

    private File ROOT_FOLDER, TEMP_FOLDER, CONFIG_FOLDER;
    private Exception exception;

    /**
     *
     * @param urls
     * @return
     */
    protected File doInBackground(String... urls) {
        try {
            // make sure appropriate folders are available
            initializeStorage();

            // URL should point to a single file or ZIP of files
            URL url = new URL(urls[0]);

            // download and dump to local storage on phone
            InputStream in = new BufferedInputStream(url.openStream(), 1024);
            File temp = File.createTempFile("temp", ".zip", TEMP_FOLDER);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
            copyInputStream(in, out);
            out.close();

            // return the temp file
            return temp;
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }

    /**
     *
     * @param file
     */
    protected void onPostExecute(File file) {
        if (exception == null) {
            try {
                // Verify the download
                String type = getMimeType(file.getPath());
                Log.e("INFO", "Config package downloaded.  Received type: " + type);

                // unzip the package
                unpackArchive(file, CONFIG_FOLDER);

                // purge the temp directory
                File[] tempFiles = TEMP_FOLDER.listFiles();
                for (File tempFile : tempFiles) tempFile.delete();
                TEMP_FOLDER.delete();
            } catch (IOException ex) {
                Log.e("ERROR", "Error extracting downloaded config package: " + ex.getMessage());
            }
        } else {
            Log.e("ERROR", exception.getMessage());
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIVATE REFERENCE METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     *
     */
    private void initializeStorage() {
        ROOT_FOLDER = new File(Environment.getExternalStorageDirectory(),
                Constants.ROOT_FOLDER_NAME);
        if (!ROOT_FOLDER.exists()) { ROOT_FOLDER.mkdirs(); }
        TEMP_FOLDER = new File(ROOT_FOLDER, Constants.TEMP_FOLDER_NAME);
        if (!TEMP_FOLDER.exists()) { TEMP_FOLDER.mkdirs(); }
        CONFIG_FOLDER = new File(ROOT_FOLDER, Constants.CONFIG_FOLDER_NAME);
        if (!CONFIG_FOLDER.exists()) { CONFIG_FOLDER.mkdirs(); }
    }

    /**
     *
     * @param in
     * @param out
     * @throws IOException
     */
    private static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len >= 0) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
        in.close();
        out.close();
    }

    /**
     *
     * @param theFile
     * @param targetDir
     * @return
     * @throws IOException
     */
    private static File unpackArchive(File theFile, File targetDir) throws IOException {
        if (!theFile.exists()) {
            throw new IOException(theFile.getAbsolutePath() + " does not exist");
        }
        if (!buildDirectory(targetDir)) {
            throw new IOException("Could not create directory: " + targetDir);
        }
        ZipFile zipFile = new ZipFile(theFile);
        for (Enumeration entries = zipFile.entries(); entries.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            File file = new File(targetDir, File.separator + entry.getName());
            if (!buildDirectory(file.getParentFile())) {
                throw new IOException("Could not create directory: " + file.getParentFile());
            }
            if (!entry.isDirectory()) {
                copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
            } else {
                if (!buildDirectory(file)) {
                    throw new IOException("Could not create directory: " + file);
                }
            }
        }
        zipFile.close();
        return theFile;
    }

    /**
     *
     * @param file
     * @return
     */
    private static boolean buildDirectory(File file) {
        return file.exists() || file.mkdirs();
    }

    /**
     *
     * @param url
     * @return
     */
    private static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

}