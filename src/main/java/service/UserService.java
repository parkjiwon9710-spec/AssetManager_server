package service;


import model.User;

public class UserService {
    private UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        return userDAO.login(username, password);
    }

    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    public void applyBalanceChange(int userId, double delta) {
        userDAO.addBalance(userId, delta);
    }

    public int changePassword(int userId, String currentPw, String newPw) {
        if (!userDAO.checkPassword(userId, currentPw)) {
            return -1;  // 현재 비번 틀림
        }
        if (newPw == null || newPw.length() < 4) {
            return -2;  // 새 비번 정책 위반
        }
        userDAO.updatePassword(userId, newPw);
        return 0;  // 성공
    }

}

