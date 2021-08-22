package com.harry9656.MusicalSpace.controllers;

import com.harry9656.MusicalSpace.dao.UsersDAO;
import com.harry9656.MusicalSpace.exceptions.InvalidCredentialException;
import com.harry9656.MusicalSpace.utils.ConnectionHandler;
import org.apache.commons.text.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(value = "/CheckLogin", name = "CheckLogin")
public class CheckLogin extends HttpServlet {
    private Connection connection = null;
    private static TemplateEngine templateEngine;

    public CheckLogin() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter responseWriter = response.getWriter();
        try {
            String userName = getEscapedPropertyStringFromRequestOrElseThrowInvalidCredentialException(request, "username");
            String psw = getEscapedPropertyStringFromRequestOrElseThrowInvalidCredentialException(request, "psw");
            UsersDAO userDao = new UsersDAO(connection);
            request.getSession().setAttribute("user", userDao.getValidatedUser(userName, psw).orElseThrow(() -> new InvalidCredentialException("Username " + userName + " not found")));
            response.sendRedirect(getServletContext().getContextPath() + "/Home");
        } catch (InvalidCredentialException invalidCredentialException) {
            ServletContext servletContext = getServletContext();
            final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
            ctx.setVariable("errorMsg", invalidCredentialException.getMessage());
            templateEngine.process("/index.html", ctx, responseWriter);
        }
    }

    private String getEscapedPropertyStringFromRequestOrElseThrowInvalidCredentialException(HttpServletRequest request, String propertyName) {
        String propertyValue;
        propertyValue = StringEscapeUtils.escapeJava(request.getParameter(propertyName));
        if (StringUtils.isEmpty(propertyValue)) {
            throw new InvalidCredentialException("Missing " + propertyName);
        }
        return propertyValue;
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException.getMessage(), sqlException);
        }
    }
}