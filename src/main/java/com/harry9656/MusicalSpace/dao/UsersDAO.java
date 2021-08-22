package com.harry9656.MusicalSpace.dao;

import com.harry9656.MusicalSpace.exceptions.InvalidCredentialException;
import com.harry9656.MusicalSpace.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UsersDAO {
    private final Connection connection;

    public UsersDAO(Connection connection) {
        this.connection = connection;
    }

    public Optional<User> getValidatedUser(String userName, String password) {
        String query = "SELECT  userId, username FROM users  WHERE username = ? AND password =?";
        try (PreparedStatement pstatement = connection.prepareStatement(query)) {
            pstatement.setString(1, userName);
            pstatement.setString(2, password);
            try (ResultSet result = pstatement.executeQuery()) {
                if (!result.isBeforeFirst())
                    throw new InvalidCredentialException("Credentials not present");
                else {
                    result.next();
                    return Optional.of(new User(
                            result.getLong("userId"),
                            result.getString("username")
                    ));
                }
            }
        } catch (SQLException exception) {
            throw new InvalidCredentialException("Unable to connect to database", exception);
        }
    }
}