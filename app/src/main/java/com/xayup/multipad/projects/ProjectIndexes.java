package com.xayup.multipad.projects;

public interface ProjectIndexes {
    public final byte PROJECT_STATE_GOOD = 0;
    public final byte PROJECT_STATE_BAD = 1;
    public final byte PROJECT_STATE_USELESS = 2;

    public final byte PROJECT_TITLE = 0;
    public final byte PROJECT_PRODUCER_NAME = 1;
    public final byte PROJECT_PATH = 2;
    public final byte PROJECT_STATE = 3;
    public final byte PROJECT_SAMPLES_PATH = 4;
    public final byte PROJECT_KEYLEDS_PATHS = 5;
    public final byte PROJECT_AUTOPLAY_PATH = 6;
    public final byte PROJECT_INFO_PATH = 7;
    public final byte PROJECT_KEYSOUND_PATH = 8;
    public final byte PROJECT_KEYLEDS_COUNT = 9;
    public final byte PROJECT_SAMPLES_COUNT = 10;
    public final byte PROJECT_DIFFICULT = 11;

    public final byte TYPE_KEYLED_FOLDERS = 0;
    public final byte TYPE_SAMPLE_FOLDER = 1;
    public final byte TYPE_AUTOPLAY_FILE = 2;

    public final byte FLAG_SAMPLE_COUNT = 3;
    public final byte FLAG_KEYLED_COUNT = 4;
    public final byte FLAG_AUTOPLAY_DIFICULTY = 5;
    public final byte FLAG_TITLE = 6;
    public final byte FLAG_PRODUCER_NAME = 7;
    public final byte FLAG_COVER = 8;
    public final byte FLAG_ITEM_SAMPLE_COUNT = 9;
    public final byte FLAG_ITEM_KEYLED_COUNT = 10;
    public final byte FLAG_ITEM_AUTOPLAY_DIFFICULTY = 11;
    public final byte FLAG_ITEM_TITLE = 12;
    public final byte FLAG_ITEM_PRODUCER_NAME = 13;
    public final byte FLAG_ITEM_COVER = 14;
    public final byte FLAG_ITEM_STATE_VIEW = 15;
    public final byte FLAG_STATE_VIEW = 16;
    public final byte FLAG_ITEM_STATE_TEXT = 17;
    public final byte FLAG_STATE_TEXT = 18;

    public final byte FLAG_SIZE = 19; /* Array flag size */
}
