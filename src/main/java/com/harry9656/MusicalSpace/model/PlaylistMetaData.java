package com.harry9656.MusicalSpace.model;

import java.util.Date;
import java.util.Objects;

public class PlaylistMetaData {
    private final long playlistId;
    private final String title;
    private final Date creationDate;
    private final long userId;

    public PlaylistMetaData(long id, String title, Date creationDate, long userId) {
        this.playlistId = id;
        this.title = title;
        this.creationDate = creationDate;
        this.userId = userId;
    }

    public long getPlaylistId() {
        return playlistId;
    }

    public String getTitle() {
        return title;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaylistMetaData that = (PlaylistMetaData) o;
        return getPlaylistId() == that.getPlaylistId() && getUserId() == that.getUserId() && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getCreationDate(), that.getCreationDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlaylistId(), getTitle(), getCreationDate(), getUserId());
    }
}
