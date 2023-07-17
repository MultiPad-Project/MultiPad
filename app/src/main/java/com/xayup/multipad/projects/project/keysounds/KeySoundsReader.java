package com.xayup.multipad.projects.project.keysounds;

import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.Log;
import com.google.common.io.Files;
import com.xayup.multipad.projects.thread.LoadProject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class KeySoundsReader {
    /**
     * @param file Sample file
     * @return an array with size 2, where index 0 is the time (in milliseconds) and index 1 is the raw file size.
     */
    protected int[] getNecessaryMetadata(File file) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        int[] rtr =  new int[]{Integer.parseInt(
                mediaMetadataRetriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_DURATION)), ((int) file.length() / 1024)};
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) mediaMetadataRetriever.release();
            else mediaMetadataRetriever.close();
        } catch (IOException io){
            Log.e("MediaMetadataRetrever", io.toString());
        }
        return rtr;
    }

    protected boolean checkKeySound(String line) {
        return line.matches(
                "([1-2][0-9]|[1-9])[1-9][0-8][\\w\\W]+.\\w{3}([0-9][1-9]|[0-9][1-2][0-9])?");
    }
    /**
     * Read KeySounds file and return list with possibles errors.
     *
     * @param key_sounds "keysounds" file
     * @param mSoundLoader Class for load samples
     */
    public void read(
            File key_sounds,
            File sample_path,
            SoundLoader mSoundLoader,
            LoadProject.LoadingProject mLoadingProject) {
        if (key_sounds.exists()) {
            try {
                List<String> keys = Files.readLines(key_sounds, StandardCharsets.UTF_8);
                mLoadingProject.onStartReadFile(key_sounds.getName());
                int line_number = 0;
                for (String line : keys) {
                    line_number++;
                    int indextheSound = 3;
                    int indexPad = 1;
                    if ((!line.replaceAll("\\s", "").isEmpty()))
                        if (checkKeySound(line.replaceAll("\\s", ""))) {
                            if (line.substring(0, 2).matches("[1-2][0-9]")) {
                                indextheSound = 4;
                                indexPad = 2;
                            }
                            line = line.replace(" ", "");
                            String ifToChain = line.substring(line.lastIndexOf(".") + 4);
                            if (ifToChain.matches("1([1-9]|[1-2][0-9])"))
                                ifToChain = ifToChain.substring(1);
                            else ifToChain = null;
                            line = line.substring(0, line.lastIndexOf(".") + 4);
                            String sound = sample_path + "/" + line.substring(indextheSound);
                            try {
                                mSoundLoader.loadSound(
                                        sound,
                                        getNecessaryMetadata(new File(sound)),
                                        line.substring(0, indextheSound),
                                        ifToChain);
                            } catch (IllegalArgumentException i) {
                                mLoadingProject.onFileError(
                                        key_sounds.getName(),
                                        line_number,
                                        keys.get(line_number - 1));
                            }
                        } else {
                            mLoadingProject.onFileError(
                                    key_sounds.getName(),
                                    line_number,
                                    keys.get(line_number - 1));
                        }
                }
            } catch (IOException e) {
                mLoadingProject.onFileError(key_sounds.getName(), 0, "Corrupted");
            }
        }
    }
}
