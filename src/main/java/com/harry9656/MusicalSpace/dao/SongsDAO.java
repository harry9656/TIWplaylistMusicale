package com.harry9656.MusicalSpace.dao;

import com.harry9656.MusicalSpace.exceptions.InvalidSongDataException;
import com.harry9656.MusicalSpace.model.SongFile;
import com.harry9656.MusicalSpace.model.SongMetaData;
import com.harry9656.MusicalSpace.model.SongThumbnail;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SongsDAO {
    private final Connection connection;

    public SongsDAO(Connection connection) {
        this.connection = connection;
    }

    public void uploadSong(SongMetaData songMetaData, SongFile songFile, SongThumbnail songThumbnail) {
        String query = "Insert into songmetadata (title, songThumbnailId, albumTitle, credit, publicationYear, genre,songFileId,userId) VALUES (?,?,?,?,?,?,?,?)";
        long songFileId = saveSongFileToDb(songFile);
        long songThumbnailId = saveSongThumbnailToDb(songThumbnail);
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, songMetaData.getTitle());
            preparedStatement.setLong(2, songThumbnailId);
            preparedStatement.setString(3, songMetaData.getAlbumTitle());
            preparedStatement.setString(4, songMetaData.getCredit());
            preparedStatement.setObject(5, songMetaData.getPublicationYear());
            preparedStatement.setString(6, songMetaData.getGenre());
            preparedStatement.setLong(7, songFileId);
            preparedStatement.setLong(8, songMetaData.getUserId());
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Unable to save songmetadata", sqlException);
        }
    }

    private long saveSongFileToDb(SongFile songFile) {
        String query = "INSERT INTO songfile (song) VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setBlob(1, new DataInputStream(new FileInputStream(songFile.getSong())));
            return saveAndGetId(preparedStatement);
        } catch (SQLException sqlException) {
            throw new RuntimeException("Problem saving the song file", sqlException);
        } catch (FileNotFoundException fileNotFoundException) {
            throw new RuntimeException("Problem getting the song file", fileNotFoundException);
        }
    }

    private long saveSongThumbnailToDb(SongThumbnail songThumbnail) {
        String query = "INSERT INTO songthumbnail (songThumbnail) VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setBlob(1, new DataInputStream(new FileInputStream(songThumbnail.getSongThumbnailFile())));
            return saveAndGetId(preparedStatement);
        } catch (SQLException sqlException) {
            throw new RuntimeException("Problem saving the song file", sqlException);
        } catch (FileNotFoundException fileNotFoundException) {
            throw new RuntimeException("Problem getting the song file", fileNotFoundException);
        }
    }

    private long saveAndGetId(PreparedStatement preparedStatement) throws SQLException {
        int affectedRowCount = preparedStatement.executeUpdate();
        if (affectedRowCount == 0) {
            throw new RuntimeException("File was not saved");
        } else {
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new IllegalStateException("Unable to find the inserted Id of the file");
                }
            }
        }
    }

    public List<SongMetaData> getSongsMetaDataById(List<Long> songIds) {
        if (songIds.isEmpty()) {
            return new ArrayList<>();
        }
        String queryPlaceHolder = "SELECT songId, title,  songThumbnailId,  albumTitle,  credit,  publicationYear,  genre,  songFileId,  userId FROM songmetadata WHERE songId in (%s)";
        String query = String.format(queryPlaceHolder, IntStream.range(1, songIds.size() + 1).mapToObj(integer -> "?").collect(Collectors.joining(",")));
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            for (int i = 1; i < songIds.size() + 1; i++) {
                pstatement.setLong(i, songIds.get(i - 1));
            }
            try (ResultSet result = pstatement.executeQuery()) {
                List<SongMetaData> songMetaDataList = new ArrayList<>();
                while (result.next()) {
                    songMetaDataList.add(new SongMetaData(
                            result.getLong("songId"),
                            result.getString("title"),
                            result.getLong("songThumbnailId"),
                            result.getString("albumTitle"),
                            result.getString("credit"),
                            result.getDate("publicationYear").toLocalDate(),
                            result.getString("genre"),
                            result.getLong("songFileId"),
                            result.getLong("userId")
                    ));
                }
                return songMetaDataList;

            } catch (IOException e) {
                throw new InvalidSongDataException("Unable to load song data");
            }
        } catch (
                SQLException exception) {
            throw new InvalidSongDataException("Unable to connect to database", exception);
        }

    }

    public List<SongMetaData> getSongsMetaDataByUserId(Long userId) {
        String query = "SELECT songId, title,  songThumbnailId,  albumTitle,  credit,  publicationYear,  genre,  songFileId,  userId FROM songmetadata WHERE userId = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setLong(1, userId);
            try (ResultSet result = pstatement.executeQuery()) {
                List<SongMetaData> songMetaDataList = new ArrayList<>();
                while (result.next()) {
                    songMetaDataList.add(new SongMetaData(
                            result.getLong("songId"),
                            result.getString("title"),
                            result.getLong("songThumbnailId"),
                            result.getString("albumTitle"),
                            result.getString("credit"),
                            result.getDate("publicationYear").toLocalDate(),
                            result.getString("genre"),
                            result.getLong("songFileId"),
                            result.getLong("userId")
                    ));
                }
                return songMetaDataList;
            } catch (IOException e) {
                throw new InvalidSongDataException("Unable to load song data");
            }
        } catch (SQLException exception) {
            throw new InvalidSongDataException("Unable to connect to database", exception);
        }
    }

    public SongFile getSongFileById(long songFileId) {
        String query = "SELECT songFileId, song FROM songfile WHERE songFileId =?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setLong(1, songFileId);
            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst())
                    throw new InvalidSongDataException("Unable to find song file");
                else {
                    result.next();
                    return new SongFile(result.getLong("songFileId"),
                            result.getBinaryStream("song"));
                }
            } catch (IOException e) {
                throw new InvalidSongDataException("Unable to load song file");
            }
        } catch (SQLException exception) {
            throw new InvalidSongDataException("Unable to connect to database", exception);
        }
    }

    public SongThumbnail getSongThumbnail(long songThumbnailId) {
        String query = "SELECT songThumbnailId, songThumbnail FROM songthumbnail WHERE songThumbnailId =?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setLong(1, songThumbnailId);
            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst())
                    throw new InvalidSongDataException("Unable to find song file");
                else {
                    result.next();
                    return new SongThumbnail(result.getLong("songThumbnailId"),
                            result.getBinaryStream("songThumbnail"));
                }
            } catch (IOException e) {
                throw new InvalidSongDataException("Unable to load song thumbnail");
            }
        } catch (SQLException exception) {
            throw new InvalidSongDataException("Unable to connect to database", exception);
        }
    }
}
