package com.harry9656.MusicalSpace.controllers;

import com.harry9656.MusicalSpace.dao.PlaylistDAO;
import com.harry9656.MusicalSpace.model.User;
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

@WebServlet(name = "CreateEmptyPlaylistPage", value = "/CreateEmptyPlaylistPage")
public class CreateEmptyPlaylistPage extends HttpServlet {
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
        long userId = ((User) session.getAttribute("user")).getId();
        Optional.ofNullable(request.getParameter("playlistTitle"))
                .filter(StringUtils::isNotBlank)
                .ifPresent((playlistTitle) -> {
                    PlaylistDAO playlistDAO = new PlaylistDAO(connection);
                    playlistDAO.createEmptyPlaylist(playlistTitle, userId);
                });
        response.sendRedirect(getServletContext().getContextPath() + "/Home");
    }
}
