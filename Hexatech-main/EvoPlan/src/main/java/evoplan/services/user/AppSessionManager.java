package evoplan.services.user;

import evoplan.entities.user.User;

public class AppSessionManager {
    private static AppSessionManager instance;
    private User currentUser;
    private String userRole;
    private String googleAuthRegisterMail;
    private AppSessionManager() { }

    public static AppSessionManager getInstance() {
        if (instance == null) {
            instance = new AppSessionManager();
        }
        return instance;
    }

    public void login(User user, String role) {
        this.currentUser = user;
        this.userRole = role;
    }

    public void setGoogleAuthRegisterMail(String googleAuthRegisterMail) {
        this.googleAuthRegisterMail = googleAuthRegisterMail;
    }

    public String getGoogleAuthRegisterMail(){return this.googleAuthRegisterMail;}
    public User getCurrentUser() {
        return currentUser;
    }

    public Integer getCurrentUserId() {
        return (currentUser != null) ? currentUser.getId() : null;
    }

    public String getUserRole() {
        return userRole;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
        userRole = null;
    }

    public String getCurrentUserName() {
        return (currentUser != null) ? currentUser.getName() : null;
    }

    public String getCurrentUserEmail() {
        return (currentUser != null) ? currentUser.getEmail() : null;
    }
}
