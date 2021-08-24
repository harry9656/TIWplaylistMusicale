package com.harry9656.MusicalSpace.controllers;

import com.harry9656.MusicalSpace.dao.SongsDAO;
import com.harry9656.MusicalSpace.exceptions.InvalidSongDataException;
import com.harry9656.MusicalSpace.model.SongFile;
import com.harry9656.MusicalSpace.model.SongMetaData;
import com.harry9656.MusicalSpace.model.SongThumbnail;
import com.harry9656.MusicalSpace.model.User;
import com.harry9656.MusicalSpace.utils.ConnectionHandler;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

@WebServlet(value = "/uploadSong", name = "uploadSong")
@MultipartConfig
public class UploadSong extends HttpServlet {
    public UploadSong() {
        super();
    }

    private Connection connection = null;

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {
            response.sendRedirect(getServletContext().getContextPath() + "/index.html");
            return;
        }
        try {
            SongFile songFile = new SongFile(getFileFromRequest(request, "songFile", "audio/mpeg")
                    .orElseThrow(() -> new InvalidSongDataException("Song File not provided")));
            SongThumbnail songThumbnail = new SongThumbnail(getFileFromRequest(request, "songThumbnail", "image/jpeg")
                    .orElseThrow(() -> new InvalidSongDataException("Song Thumbnail not provided")));
            SongMetaData songMetaData = new SongMetaData(
                    getParameterOrThrowExceptionIfEmpty(request, "title").orElseThrow(() -> new InvalidSongDataException("Title not provided")),
                    getParameterOrThrowExceptionIfEmpty(request, "albumTitle").orElseThrow(() -> new InvalidSongDataException("Album title not provided")),
                    getParameterOrThrowExceptionIfEmpty(request, "credit").orElseThrow(() -> new InvalidSongDataException("Credit not provided")),
                    LocalDate.parse(getParameterOrThrowExceptionIfEmpty(request, "publicationYear")
                            .orElseThrow(() -> new InvalidSongDataException("Unable to read provided date"))),
                    getParameterOrThrowExceptionIfEmpty(request, "genre").orElseThrow(() -> new InvalidSongDataException("Genre not provided")),
                    ((User) session.getAttribute("user")).getId());
            SongsDAO songsDAO = new SongsDAO(connection);
            songsDAO.uploadSong(songMetaData, songFile, songThumbnail);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("ok");
        } catch (InvalidSongDataException invalidSongDataException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("ko");
        }
    }

    private Optional<File> getFileFromRequest(HttpServletRequest request, String partName, String mimetype) {
        try {
            Part filePart = request.getPart(partName);
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            InputStream fileContent = filePart.getInputStream();
            File tempFile = File.createTempFile("temp", fileName);
            FileUtils.copyInputStreamToFile(fileContent, tempFile);
            if (!mimetype.equals(getServletContext().getMimeType(tempFile.getCanonicalPath()))) {
                throw new InvalidSongDataException("Upload correct file types");
            }
            return Optional.of(tempFile);
        } catch (IOException | ServletException exception) {
            log("Unable to upload file", exception);
            return Optional.empty();
        }
    }

    private Optional<String> getParameterOrThrowExceptionIfEmpty(HttpServletRequest request, String title) {
        return Optional.ofNullable(request.getParameter(title));
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
