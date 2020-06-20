/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;

/**
 *
 * @author boba
 */
public class User implements Serializable{
    String userName;
    String password;
    int number;

    public User() {
    }

    public User(String userName, String password, int number) {
        this.userName = userName;
        this.password = password;
        this.number = number;
    }
    
    
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "User{" + "userName=" + userName + ", password=" + password + ", number=" + number + '}';
    }
    
    
    
}
