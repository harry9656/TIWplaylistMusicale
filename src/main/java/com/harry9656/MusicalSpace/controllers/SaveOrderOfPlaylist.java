package com.harry9656.MusicalSpace.controllers;

import com.google.gson.Gson;
import com.harry9656.MusicalSpace.dao.PlaylistDAO;
import com.harry9656.MusicalSpace.model.PlaylistSongs;
import com.harry9656.MusicalSpace.utils.ConnectionHandler;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;

@WebServlet(value = "/SaveOrderOfPlaylist", name = "SaveOrderOfPlaylist")
@MultipartConfig
public class SaveOrderOfPlaylist extends HttpServlet {
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
        Gson gson = new Gson();
        PlaylistSongs[] playlistSongs = gson.fromJson(request.getParameter("playlistSongs"), PlaylistSongs[].class);
        PlaylistDAO playlistDAO = new PlaylistDAO(connection);
        playlistDAO.updatePlaylistOrder(Arrays.asList(playlistSongs));
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("Ok");

    }
}
