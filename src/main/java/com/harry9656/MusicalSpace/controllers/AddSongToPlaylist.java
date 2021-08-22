package com.harry9656.MusicalSpace.controllers;

import com.harry9656.MusicalSpace.dao.PlaylistDAO;
import com.harry9656.MusicalSpace.exceptions.InvalidPlaylistDataException;
import com.harry9656.MusicalSpace.utils.ConnectionHandler;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.util.Optional;

@WebServlet(name = "AddSongToPlaylist", value = "/AddSongToPlaylist")
public class AddSongToPlaylist extends HttpServlet {
    private Connection connection = null;

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {
            response.sendRedirect(getServletContext().getContextPath() + "/index.html");
            return;
        }

        Long songId = Optional.ofNullable(request.getParameter("songToBeAdded"))
                .filter(StringUtils::isNotBlank)
                .map(Long::valueOf)
                .orElseThrow(() -> new InvalidPlaylistDataException("No Song was selected is empty"));
        Long playlistId = Long.valueOf(request.getParameter("playlistId"));
        PlaylistDAO playlistDAO = new PlaylistDAO(connection);
        playlistDAO.addSongToPlaylist(songId, playlistId);
        response.sendRedirect(getServletContext().getContextPath() + "/GoToPlaylistPage?playlistId=" + playlistId);

    }
}
