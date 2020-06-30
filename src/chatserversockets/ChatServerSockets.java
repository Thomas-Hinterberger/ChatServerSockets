/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserversockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Message;
import model.User;

/**
 *
 * @author agaub
 */
public class ChatServerSockets {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            ServerSocket servLogin = new ServerSocket(5000);
            ServerSocket servChat = new ServerSocket(5001);
            ServerSocket servSingUp = new ServerSocket(5002);
            ServerSocket servgetAllUser = new ServerSocket(5003);

            Thread thLogin = new Thread() {
                @Override
                public void run() {
                    try {
                        super.run();
                        ExecutorService ex = Executors.newFixedThreadPool(5000);
                        while (true) {
                            Socket ss = servLogin.accept();

                            Thread thLoginUn = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        super.run();
                                        ObjectInputStream ois = new ObjectInputStream(ss.getInputStream());
                                        User user = (User) ois.readObject();
                                        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Chat", "Test", "Test");

                                        PreparedStatement pr = con.prepareStatement("Select USERNAME, PASSWORD,NUMBER from ACCOUNT WHERE USERNAME = ? and PASSWORD = ?");

                                        pr.setString(1, user.getUserName());

                                        pr.setString(2, user.getPassword());

                                        ResultSet rs = pr.executeQuery();
                                        rs.next();
                                        if (rs.getString("Password") != null) {
                                            ObjectOutputStream oos = new ObjectOutputStream(ss.getOutputStream());
                                            oos.writeObject(new User(rs.getString("USERNAME"), rs.getString("password"), rs.getInt("number")));
                                        } else {
                                            ObjectOutputStream oos = new ObjectOutputStream(ss.getOutputStream());
                                            oos.writeObject(null);
                                        }
                                        ois.close();

                                    } catch (SQLException ex1) {
                                        ObjectOutputStream oos = null;
                                        try {
                                            oos = new ObjectOutputStream(ss.getOutputStream());
                                            oos.writeObject(null);
                                        } catch (IOException ex2) {
                                            Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex2);
                                        } finally {
                                            try {
                                                oos.flush();
                                                oos.close();
                                            } catch (IOException ex2) {
                                                Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex2);
                                            }
                                        }
                                    } catch (IOException ex1) {
                                        Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                                    } catch (ClassNotFoundException ex1) {
                                        Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                                    }
                                }

                            };

                            ex.execute(thLoginUn);
                        }

                    } catch (IOException ex1) {
                        Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }

            };

            Thread thChat = new Thread() {
                @Override
                public void run() {
                    ExecutorService ex = Executors.newFixedThreadPool(5000);
                    while (true) {
                        try {
                            super.run();
                            Socket ss = servChat.accept();
                            Thread th = new Thread() {

                                @Override
                                public void run() {
                                    ObjectInputStream ois = null;
                                    try {
                                        super.run();
                                        ois = new ObjectInputStream(ss.getInputStream());
                                        Message me = (Message) ois.readObject();
                                        DatagramSocket socket = new DatagramSocket();
                                        socket.connect(InetAddress.getByName("8.8.8.8"), me.getTo());
                                        String ip = socket.getLocalAddress().getHostAddress();

                                        Socket s = new Socket(ip, me.getTo());
                                        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                                        oos.writeObject(me);
                                        oos.flush();
                                        oos.close();
                                    } catch (IOException ex1) {
                                        Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                                    } catch (ClassNotFoundException ex1) {
                                        Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                                    } finally {
                                        try {

                                            ois.close();
                                        } catch (IOException ex1) {
                                            Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                                        }
                                    }

                                }

                            };
                            ex.execute(th);
                        } catch (IOException ex1) {
                            Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                        }

                    }

                }

            };

            Thread thSignUp = new Thread() {
                @Override
                public void run() {
                    try {
                        super.run();
                        ExecutorService ex = Executors.newFixedThreadPool(5000);
                        while (true) {
                            Socket s = servSingUp.accept();
                            Thread th = new Thread() {
                                @Override
                                public void run() {
                                    ObjectInputStream ois = null;
                                    try {
                                        super.run();
                                        ois = new ObjectInputStream(s.getInputStream());
                                        User us = (User) ois.readObject();
                                        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Chat", "Test", "Test");

                                        PreparedStatement pr2 = con.prepareStatement("Select number from ACCOUNT order by number DESC");
                                        ResultSet set = pr2.executeQuery();
                                        set.next();
                                        PreparedStatement pr = con.prepareStatement("insert into ACCOUNT values (?,?,?)");
                                        pr.setString(1, us.getUserName());
                                        pr.setInt(2, set.getInt(1) + 1);
                                        pr.setString(3, us.getPassword());
                                        pr.executeUpdate();
                                        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                                        oos.writeObject(us);
                                        ois.close();
                                        oos.flush();
                                        oos.close();

                                    } catch (IOException ex) {
                                        Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (ClassNotFoundException ex) {
                                        Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex);
                                    } catch (SQLException ex) {
                                        ObjectOutputStream oos = null;
                                        try {
                                            oos = new ObjectOutputStream(s.getOutputStream());
                                            oos.writeObject(null);
                                            oos.close();
                                        } catch (IOException ex1) {
                                            Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                                        } finally {
                                            try {
                                                oos.close();
                                            } catch (IOException ex1) {
                                                Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                                            }
                                        }

                                    } finally {
                                        try {
                                            ois.close();
                                        } catch (IOException ex) {
                                            Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }

                                }

                            };
                            ex.execute(th);

                        }

                    } catch (IOException ex) {
                        Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            };

            Thread getAllUser = new Thread() {
                @Override
                public void run() {
                    try {
                        super.run();
                        ExecutorService ex = Executors.newFixedThreadPool(5000);
                        while (true) {
                            Socket ss = servgetAllUser.accept();

                            Thread thLoginUn = new Thread() {
                                @Override
                                public void run() {
                                    ArrayList<User> arrperson = new ArrayList<User>();
                                    try {
                                        Connection con = DriverManager.getConnection("jdbc:derby://localhost:1527/Chat", "Test", "Test");
                                        Statement f = con.createStatement();
                                        String sql = "Select * from Account";
                                        ResultSet set = f.executeQuery(sql);

                                        while (set.next()) {
                                            String user = set.getString(1);
                                            int port = set.getInt(2);
                                            String password = set.getString(3);
                                            User p = new User(user, password, port);
                                            arrperson.add(p);
                                        }
                                        con.close();
                                        ObjectOutputStream oos = new ObjectOutputStream(ss.getOutputStream());
                                        oos.writeObject(arrperson);
                                        oos.flush();
                                        oos.close();

                                    } catch (IOException ex1) {
                                        Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                                    } catch (SQLException ex1) {
                                        Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                                    }

                                }

                            };

                            ex.execute(thLoginUn);
                        }

                    } catch (IOException ex1) {
                        Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }

            };

            thSignUp.start();
            thChat.start();
            thLogin.start();
            getAllUser.start();

        } catch (IOException ex) {
            Logger.getLogger(ChatServerSockets.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
