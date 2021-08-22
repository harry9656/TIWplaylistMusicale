package com.harry9656.MusicalSpace.model;

import java.util.Objects;

public class PlaylistSongs {
    private final long playlistId;
    private final long songId;
    private final long orderWeight;

    public PlaylistSongs(long playlistId, long songId, long order) {
        this.playlistId = playlistId;
        this.songId = songId;
        this.orderWeight = order;
    }

    public long getPlaylistId() {
        return playlistId;
    }

    public long getSongId() {
        return songId;
    }

    public long getOrderWeight() {
        return orderWeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaylistSongs that = (PlaylistSongs) o;
        return getPlaylistId() == that.getPlaylistId() && getSongId() == that.getSongId() && getOrderWeight() == that.getOrderWeight();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlaylistId(), getSongId(), getOrderWeight());
    }
}
