package com.harry9656.MusicalSpace.model;

import java.util.Objects;

public class PlaylistSongs {
    private final long playlistId;
    private final long songId;

    public PlaylistSongs(long playlistId, long songId) {
        this.playlistId = playlistId;
        this.songId = songId;
    }

    public long getPlaylistId() {
        return playlistId;
    }

    public long getSongId() {
        return songId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaylistSongs that = (PlaylistSongs) o;
        return getPlaylistId() == that.getPlaylistId() && getSongId() == that.getSongId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlaylistId(), getSongId());
    }
}
