package com.example.lotterize;

/**
 * Models a User. Contains all attributes of a user.
 */
public class User {

    private String name;
    private String userId;
    private String phoneNumber;
    private String email;
    private String coordinates;
    private String username;
    private String password;

    public User(String userId, String name, String phoneNumber, String email,
                String coordinates, String username, String password) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.coordinates = coordinates;
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * @return String - name of user
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @return String - email of user
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return String - coordinates of user
     */
    public String getCoordinates() {
        return coordinates;
    }

    /**
     * @return String - id of user
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return String - username of user
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return String - password of user
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param userId - new id for the user
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}