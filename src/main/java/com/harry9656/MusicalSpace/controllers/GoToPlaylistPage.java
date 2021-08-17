package com.harry9656.MusicalSpace.controllers;

import com.harry9656.MusicalSpace.dao.PlaylistDAO;
import com.harry9656.MusicalSpace.dao.SongsDAO;
import com.harry9656.MusicalSpace.model.PlaylistMetaData;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.harry9656.MusicalSpace.model.CONSTANTS.PLAYLIST_TEMPLATE;

@WebServlet(value = "/GoToPlaylistPage", name = "GoToPlaylistPage")
public class GoToPlaylistPage extends HttpServlet {
    private TemplateEngine templateEngine;
    private Connection connection = null;

    public GoToPlaylistPage() {
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
        User loggedUser = (User) session.getAttribute("user");
        ctx.setVariable("username", loggedUser.getUserName());
        PlaylistDAO playlistDAO = new PlaylistDAO(connection);
        List<Long> songsId = playlistDAO.getSongIdListByPlaylistId(Long.parseLong(request.getParameter("playlistId")));
        Long offset = Optional.ofNullable(request.getParameter("offset")).map(Long::parseLong).orElse(0L);
        SongsDAO songsDAO = new SongsDAO(connection);
        if (offset <= 0 || songsId.isEmpty()) {
            offset = 0L;
            ctx.setVariable("hasPrevious", false);
        } else {
            ctx.setVariable("hasPrevious", true);
        }
        if (offset > songsId.size() || songsId.isEmpty()) {
            offset = Long.valueOf(songsId.size());
            ctx.setVariable("hasNext", false);
        } else {
            ctx.setVariable("hasNext", true);
        }
        List<SongMetaData> songs = songsDAO.getSongsMetaDataById(songsId.stream().skip(offset).limit(5).collect(Collectors.toList()));
        if (songs.size() < 5 || offset + 5 >= songsId.size()) {
            ctx.setVariable("hasNext", false);
        }
        songs.sort(Comparator.comparing(SongMetaData::getPublicationYear));
        Collections.reverse(songs);
        ctx.setVariable("songs", songs);
        ctx.setVariable("playlistTitle", playlistDAO.getPlaylistById(Long.parseLong(request.getParameter("playlistId"))).map(PlaylistMetaData::getTitle).orElse("Untitled"));
        ctx.setVariable("playlistId", Long.parseLong(request.getParameter("playlistId")));
        ctx.setVariable("previousOffset", offset);
        if (songsId.isEmpty()) {
            ctx.setVariable("emptyMessage", "This playlist does not contain songs");
        }
        List<SongMetaData> songsMetaDataByUserId = songsDAO.getSongsMetaDataByUserId(loggedUser.getId())
                .stream().filter(songMetaData -> !songsId.contains(songMetaData.getSongId())).collect(Collectors.toList());
        ctx.setVariable("userSongs", songsMetaDataByUserId);
        if (songsMetaDataByUserId.isEmpty()) {
            ctx.setVariable("hasSongsThatCanBeAdded", false);
        } else {
            ctx.setVariable("hasSongsThatCanBeAdded", true);
        }
        templateEngine.process(PLAYLIST_TEMPLATE, ctx, response.getWriter());
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
