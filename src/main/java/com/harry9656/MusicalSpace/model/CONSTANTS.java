package com.harry9656.MusicalSpace.model;

public class CONSTANTS {
    public static final String HOME_TEMPLATE = "/WEB-INF/Home.html";
    public static final String PLAYLIST_TEMPLATE = "/WEB-INF/PlaylistPage.html";
    public static final String SONG_TEMPLATE = "/WEB-INF/SongPage.html";

    public enum GENRES {
        JAZZ("Jazz"), HIPHOP("Hiphop"), CLASSIC("Classic");
        private final String name;

        GENRES(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
