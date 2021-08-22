package com.harry9656.MusicalSpace.controllers;

import com.harry9656.MusicalSpace.dao.UsersDAO;
import com.harry9656.MusicalSpace.exceptions.InvalidCredentialException;
import com.harry9656.MusicalSpace.model.User;
import com.harry9656.MusicalSpace.utils.ConnectionHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(value = "/CheckLogin", name = "CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
    private Connection connection = null;

    public CheckLogin() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        PrintWriter responseWriter = response.getWriter();
        try {
            String userName = getEscapedPropertyStringFromRequestOrElseThrowInvalidCredentialException(request, "username");
            String psw = getEscapedPropertyStringFromRequestOrElseThrowInvalidCredentialException(request, "psw");
            UsersDAO userDao = new UsersDAO(connection);
            User validatedUser = userDao.getValidatedUser(userName, psw).orElseThrow(() -> new InvalidCredentialException("Username " + userName + " not found"));
            request.getSession().setAttribute("user", validatedUser);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            responseWriter.println(validatedUser.getUserName());
        } catch (InvalidCredentialException invalidCredentialException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseWriter.println(invalidCredentialException.getMessage());
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