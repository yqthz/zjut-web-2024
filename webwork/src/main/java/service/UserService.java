package service;

import dao.UserDao;
import model.User;

import java.util.Date;
import java.util.List;

public class UserService {
    private UserDao userDao = new UserDao();
    public void update(User user) {
        userDao.update(user);
    }

    public void add(User user) {
        userDao.add(user);
    }

    public List<User> selectAll() {
        return userDao.selectUser();
    }

    public void delete(int userId) {
        userDao.delete(userId);
    }
    // 用户登录失败次数加 1
    public void addone(String username) {
        userDao.addone();
    }

    // 获取用户失败次数
    public int getFailNumber(String username) {
        return userDao.getFailNumber();
    }

    // 锁定账户
    public void lockAccount(String username) {
        userDao.setAccountLockedUntil(new Date(System.currentTimeMillis() + 30 * 60 * 1000)); // 锁定30分钟
    }
    // 重置失败次数
    public void resetFailNumber(String username) {
        userDao.resetFailNumber(username);
    }
    // 判断账户是否锁定
    public boolean isAccountLocked(String username) {
        Date accountLockTime = userDao.getAccountLockUntil(username);
        // 检查 accountLockTime 是否在当前时间之后（即，检查账户锁定时间是否还没有过期）
        // new Date() 表示当前时间
        // 如果 accountLockTime 在当前时间之后，after 方法返回 true，否则返回 false
        return accountLockTime.after(new Date());
    }
}
