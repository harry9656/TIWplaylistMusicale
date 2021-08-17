package com.harry9656.MusicalSpace.model;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

public class SongMetaData {
    private final long songId;
    private final String title;
    private final long thumbnailId;
    private final String albumTitle;
    private final String credit;
    private final LocalDate publicationYear;
    private final String genre;
    private final long songFileId;
    private final long userId;

    public SongMetaData(long id, String title, long thumbnailId, String albumTitle, String credit, LocalDate publicationYear, String genre, long songFileId, long userId) throws IOException {
        this.songId = id;
        this.title = title;
        this.thumbnailId = thumbnailId;
        this.albumTitle = albumTitle;
        this.credit = credit;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.songFileId = songFileId;
        this.userId = userId;
    }

    public SongMetaData(String title, String albumTitle, String credit, LocalDate publicationYear, String genre, long userId) {
        this.songId = -1L;
        this.title = title;
        this.thumbnailId = -1;
        this.albumTitle = albumTitle;
        this.credit = credit;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.songFileId = -1L;
        this.userId = userId;
    }

    public long getSongId() {
        return songId;
    }

    public String getTitle() {
        return title;
    }

    public long getThumbnailId() {
        return thumbnailId;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public String getCredit() {
        return credit;
    }

    public LocalDate getPublicationYear() {
        return publicationYear;
    }

    public String getGenre() {
        return genre;
    }

    public long getSongFileId() {
        return songFileId;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongMetaData that = (SongMetaData) o;
        return getSongId() == that.getSongId() && getSongFileId() == that.getSongFileId() && getUserId() == that.getUserId() && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getThumbnailId(), that.getThumbnailId()) && Objects.equals(getAlbumTitle(), that.getAlbumTitle()) && Objects.equals(getCredit(), that.getCredit()) && Objects.equals(getPublicationYear(), that.getPublicationYear()) && Objects.equals(getGenre(), that.getGenre());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSongId(), getTitle(), getThumbnailId(), getAlbumTitle(), getCredit(), getPublicationYear(), getGenre(), getSongFileId(), getUserId());
    }
}
