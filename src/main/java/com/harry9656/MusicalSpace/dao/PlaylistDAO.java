package com.harry9656.MusicalSpace.dao;

import com.harry9656.MusicalSpace.exceptions.InvalidPlaylistDataException;
import com.harry9656.MusicalSpace.model.PlaylistMetaData;
import com.harry9656.MusicalSpace.model.PlaylistSongs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PlaylistDAO {
    private final Connection connection;

    public PlaylistDAO(Connection connection) {
        this.connection = connection;
    }

    public void createEmptyPlaylist(String playlistName, long userId) {
        String query = "INSERT INTO playlistmetadata (title, creationDate, userId) VALUES (?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, playlistName);
            preparedStatement.setObject(2, LocalDate.now());
            preparedStatement.setLong(3, userId);
            int affectedRowCount = preparedStatement.executeUpdate();
            if (affectedRowCount == 0) {
                throw new InvalidPlaylistDataException("No Playlist was created");
            }
        } catch (SQLException sqlException) {
            throw new InvalidPlaylistDataException("Problem creating new playlist with name|" + playlistName + "|for userId|" + userId, sqlException);
        }
    }

    public List<PlaylistMetaData> getAllPlaylistsByUserIdOrderedByCreationDate(long userId) {
        String query = "SELECT playlistId, title, creationDate, userId FROM playlistmetadata WHERE userId=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<PlaylistMetaData> playlistMetaDataList = new ArrayList<>();
                while (resultSet.next()) {
                    playlistMetaDataList.add(new PlaylistMetaData(
                            resultSet.getLong("playlistId"),
                            resultSet.getString("title"),
                            resultSet.getDate("creationDate"),
                            resultSet.getLong("userId")
                    ));
                }
                playlistMetaDataList.sort(Comparator.comparing(PlaylistMetaData::getCreationDate));
                return playlistMetaDataList;
            }
        } catch (SQLException sqlException) {
            throw new InvalidPlaylistDataException("Problem getting playlist of user|" + userId, sqlException);
        }
    }

    public List<PlaylistSongs> getPlaylistSongsListByPlaylistId(long playlistId) {
        String query = "SELECT playlistId, songId, orderWeight FROM playlistsongs WHERE playlistId=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, playlistId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<PlaylistSongs> songsInPlaylist = new ArrayList<>();
                while (resultSet.next()) {
                    PlaylistSongs playlistSong = new PlaylistSongs(resultSet.getLong("playlistId"),
                            resultSet.getLong("songId"),
                            resultSet.getLong("orderWeight"));
                    songsInPlaylist.add(playlistSong);
                }
                return songsInPlaylist;
            }
        } catch (SQLException sqlException) {
            throw new InvalidPlaylistDataException("Unable to load songs of playlist|" + playlistId, sqlException);
        }
    }

    public void addSongToPlaylist(long songId, long playlistId) {
        String query = "INSERT INTO playlistsongs (playlistId, songId) VALUES (?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, playlistId);
            preparedStatement.setLong(2, songId);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new InvalidPlaylistDataException("No Song was not added to the playlist");
            }
        } catch (SQLException sqlException) {
            throw new InvalidPlaylistDataException("Unable to add song in playlist|" + playlistId, sqlException);
        }
    }

    public void updatePlaylistOrder(List<PlaylistSongs> songs) {
        String query = "UPDATE musicalspace.playlistsongs t SET t.orderWeight = ? WHERE t.playlistId = ? AND t.songId = ?";
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = null;
            preparedStatement = connection.prepareStatement(query);
            for (PlaylistSongs song : songs) {
                preparedStatement.setLong(1, song.getOrderWeight());
                preparedStatement.setLong(2, song.getPlaylistId());
                preparedStatement.setLong(3, song.getSongId());
                preparedStatement.addBatch();
            }
            int[] count = preparedStatement.executeBatch();
            connection.commit();
        } catch (SQLException exception) {
            throw new InvalidPlaylistDataException("Unable to update the order of playlist");
        }


    }
}
