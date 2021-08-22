package com.harry9656.MusicalSpace.model;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class SongFile {
    private final long id;
    private final File song;

    public SongFile(long id, InputStream songInputStream) throws IOException {
        this.id = id;
        File songFile = File.createTempFile("songFile", String.valueOf(id));
        FileUtils.copyInputStreamToFile(songInputStream, songFile);
        this.song = songFile;
        this.song.deleteOnExit();
    }


    public SongFile(File song) {
        this.id = -1L;
        this.song = song;
    }

    public long getId() {
        return id;
    }

    public File getSong() {
        return song;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongFile songFile = (SongFile) o;
        return id == songFile.id && Objects.equals(song, songFile.song);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, song);
    }
}
