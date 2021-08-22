package com.harry9656.MusicalSpace.controllers;

import com.google.gson.Gson;
import com.harry9656.MusicalSpace.dao.PlaylistDAO;
import com.harry9656.MusicalSpace.model.PlaylistMetaData;
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
import java.util.List;

@WebServlet(value = "/GetPlaylists", name = "GetPlaylists")
public class GetPlaylists extends HttpServlet {

    private Connection connection = null;

    public GetPlaylists() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null || !(session.getAttribute("user") instanceof User)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().close();
            return;
        }
        User loggedUser = (User) session.getAttribute("user");
        PlaylistDAO playlistDAO = new PlaylistDAO(connection);
        List<PlaylistMetaData> allPlaylistsByUserIdOrderedByCreationDate = playlistDAO.getAllPlaylistsByUserIdOrderedByCreationDate(loggedUser.getId());
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        Gson gson = new Gson();
        gson.toJson(allPlaylistsByUserIdOrderedByCreationDate);
        response.getWriter().println(gson.toJson(allPlaylistsByUserIdOrderedByCreationDate));
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