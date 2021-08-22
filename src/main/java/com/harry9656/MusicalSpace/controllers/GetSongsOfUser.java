package com.harry9656.MusicalSpace.controllers;

import com.google.gson.Gson;
import com.harry9656.MusicalSpace.dao.SongsDAO;
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
import java.util.List;


@WebServlet(value = "/GetSongsOfUser", name = "GetSongsOfUser")
public class GetSongsOfUser extends HttpServlet {
    private Connection connection = null;

    public GetSongsOfUser() {
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
            response.sendRedirect(getServletContext().getContextPath() + "/index.html");
            return;
        }
        User loggedUser = (User) session.getAttribute("user");
        SongsDAO playlistDAO = new SongsDAO(connection);
        List<SongMetaData> allSongs = playlistDAO.getSongsMetaDataByUserId(loggedUser.getId());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(allSongs));
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
