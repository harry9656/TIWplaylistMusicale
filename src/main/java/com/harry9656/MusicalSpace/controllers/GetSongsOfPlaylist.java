package com.harry9656.MusicalSpace.controllers;

import com.google.gson.Gson;
import com.harry9656.MusicalSpace.dao.PlaylistDAO;
import com.harry9656.MusicalSpace.dao.SongsDAO;
import com.harry9656.MusicalSpace.model.PlaylistSongs;
import com.harry9656.MusicalSpace.model.SongMetaData;
import com.harry9656.MusicalSpace.model.User;
import com.harry9656.MusicalSpace.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(value = "/GetSongsOfPlaylist", name = "GetSongsOfPlaylist")
public class GetSongsOfPlaylist extends HttpServlet {

    private Connection connection = null;

    public GetSongsOfPlaylist() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null || !(session.getAttribute("user") instanceof User)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().close();
            return;
        }
        PlaylistDAO playlistDAO = new PlaylistDAO(connection);
        List<PlaylistSongs> playlistSongs = playlistDAO.getPlaylistSongsListByPlaylistId(Long.parseLong(request.getParameter("playlistId")));
        SongsDAO songsDAO = new SongsDAO(connection);
        List<SongMetaData> songs = songsDAO.getSongsMetaDataById(playlistSongs.stream().map(PlaylistSongs::getSongId).collect(Collectors.toList()));
        Map<String, List<?>> result = new HashMap<>();
        result.put("songsMetaDataList", songs);
        result.put("playlistSongsList", playlistSongs);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        doGet(request, response);
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
