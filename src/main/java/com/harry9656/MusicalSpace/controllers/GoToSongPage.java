package com.harry9656.MusicalSpace.controllers;

import com.harry9656.MusicalSpace.dao.SongsDAO;
import com.harry9656.MusicalSpace.model.SongMetaData;
import com.harry9656.MusicalSpace.model.User;
import com.harry9656.MusicalSpace.utils.ConnectionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import static com.harry9656.MusicalSpace.model.CONSTANTS.SONG_TEMPLATE;

@WebServlet(value = "/GoToSongPage", name = "GoToSongPage")
public class GoToSongPage extends HttpServlet {
    private TemplateEngine templateEngine;
    private Connection connection = null;

    public GoToSongPage() {
        super();
    }

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session.isNew() || session.getAttribute("user") == null) {
            response.sendRedirect(getServletContext().getContextPath() + "/index.html");
            return;
        }
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        ctx.setVariable("username", ((User) session.getAttribute("user")).getUserName());
        SongsDAO songsDAO = new SongsDAO(connection);
        SongMetaData songMetaData = songsDAO.getSongsMetaDataById(Collections.singletonList(Long.parseLong(request.getParameter("songId")))).get(0);
        ctx.setVariable("song", songMetaData);
        ctx.setVariable("songFileId", songMetaData.getSongFileId());
        templateEngine.process(SONG_TEMPLATE, ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
