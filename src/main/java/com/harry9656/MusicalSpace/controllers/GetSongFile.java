package com.harry9656.MusicalSpace.controllers;

import com.harry9656.MusicalSpace.dao.SongsDAO;
import com.harry9656.MusicalSpace.exceptions.InvalidSongDataException;
import com.harry9656.MusicalSpace.model.User;
import com.harry9656.MusicalSpace.utils.ConnectionHandler;
import org.apache.commons.io.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Optional;

@WebServlet(name = "GetSongFile", value = "/GetSongFile")
public class GetSongFile extends HttpServlet {
    private Connection connection = null;

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession();
        if (session.isNew() || session.getAttribute("user") == null || !(session.getAttribute("user") instanceof User)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().close();
            return;
        }
        resp.setContentType("audio/mpeg");
        resp.setHeader("Content-disposition", "attachment; filename=song.mp3");
        Long songFileId = Optional.ofNullable(req.getParameter("songFileId"))
                .map(Long::valueOf)
                .orElseThrow(() -> new InvalidSongDataException("Song not found"));
        SongsDAO songsDAO = new SongsDAO(connection);
        File song = songsDAO.getSongFileById(songFileId).getSong();
        resp.setHeader("Content-Length", String.valueOf(FileUtils.sizeOf(song)));
        resp.setHeader("Accept-Ranges", "bytes");
        try (InputStream in = new DataInputStream(new FileInputStream(song));
             OutputStream out = resp.getOutputStream()) {

            byte[] buffer = new byte[1048];

            int numBytesRead;
            while ((numBytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, numBytesRead);
            }
        }
    }
}
