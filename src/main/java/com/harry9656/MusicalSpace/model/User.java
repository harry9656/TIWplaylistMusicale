package com.harry9656.MusicalSpace.model;

import java.util.Objects;

public class User {
    private final long id;
    private final String userName;

    public User(long id, String userName) {
        this.id = id;
        this.userName = userName;
    }

    public long getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User users = (User) o;
        return getId() == users.getId() && Objects.equals(getUserName(), users.getUserName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUserName());
    }
}
