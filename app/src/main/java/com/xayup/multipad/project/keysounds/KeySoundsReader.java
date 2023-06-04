package com.xayup.multipad.project.keysounds;

import android.media.MediaMetadataRetriever;
import com.google.common.io.Files;
import com.xayup.multipad.project.keysounds.SoundLoader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KeySoundsReader {
    protected int getDuration(File file) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        String durationStr =
                mediaMetadataRetriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_DURATION);
        /*Retorno em segundos*/
        return ((int) ((Long.parseLong(durationStr) % (1000 * 60 * 60)) % (1000 * 60) / 1000));
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
     * @return List of possible errors
     */
    public List<String[]> read(File key_sounds, File sample_path, SoundLoader mSoundLoader) {
        List<String[]> problems = new ArrayList<>();
        if (key_sounds.exists()) {
            try {
                List<String> keys = Files.readLines(key_sounds, StandardCharsets.UTF_8);
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
                            else ifToChain = "";
                            line = line.substring(0, line.lastIndexOf(".") + 4);
                            String sound = sample_path + "/" + line.substring(indextheSound);
                            try {
                                int sound_length = getDuration(new File(sound));
                                mSoundLoader.loadSound(
                                        sound,
                                        sound_length,
                                        line.substring(0, indextheSound),
                                        ifToChain);
                            } catch (IllegalArgumentException i) {
                                problems.add(
                                        new String[] {
                                            "KeySounds",
                                            key_sounds.getName(),
                                            String.valueOf(line_number),
                                            keys.get(line_number - 1)
                                        });
                            }
                        } else {
                            problems.add(
                                    new String[] {
                                        "KeySounds",
                                        key_sounds.getName(),
                                        String.valueOf(line_number),
                                        keys.get(line_number - 1)
                                    });
                        }
                }
            } catch (IOException e) {
                problems.add(new String[] {"KeySounds", key_sounds.getName(), "", "File error"});
            }
        }
        return problems;
    }
}
