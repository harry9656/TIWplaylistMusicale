package com.harry9656.MusicalSpace.model;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class SongThumbnail {
    private final File songThumbnailFile;
    private final long id;

    public SongThumbnail(File songThumbnailFile) {
        this.id = -1;
        this.songThumbnailFile = songThumbnailFile;
    }

    public SongThumbnail(long songFileId, InputStream thumbnailInputStream) throws IOException {
        this.id = songFileId;
        File songFile = File.createTempFile("songThumbnail", String.valueOf(id));
        FileUtils.copyInputStreamToFile(thumbnailInputStream, songFile);
        this.songThumbnailFile = songFile;
        this.songThumbnailFile.deleteOnExit();
    }

    public File getSongThumbnailFile() {
        return songThumbnailFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongThumbnail that = (SongThumbnail) o;
        return Objects.equals(getSongThumbnailFile(), that.getSongThumbnailFile());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSongThumbnailFile());
    }
}
