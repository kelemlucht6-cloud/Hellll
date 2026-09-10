package br.com.hellmc.login;
import java.io.File;import java.sql.*;import java.util.UUID;
public final class Database{
 private final Connection c;
 public Database(File f)throws SQLException{c=DriverManager.getConnection("jdbc:sqlite:"+f.getAbsolutePath());try(Statement s=c.createStatement()){s.executeUpdate("CREATE TABLE IF NOT EXISTS accounts(uuid TEXT PRIMARY KEY,name TEXT NOT NULL,password_hash TEXT NOT NULL,salt TEXT NOT NULL,created_at INTEGER NOT NULL,last_login INTEGER)");s.executeUpdate("CREATE TABLE IF NOT EXISTS sessions(uuid TEXT PRIMARY KEY,ip TEXT,expires_at INTEGER NOT NULL)");}}
 public boolean has(UUID u)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM accounts WHERE uuid=?")){p.setString(1,u.toString());return p.executeQuery().next();}}
 public Account get(UUID u)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT uuid,name,password_hash,salt,created_at,last_login FROM accounts WHERE uuid=?")){p.setString(1,u.toString());var r=p.executeQuery();if(!r.next())return null;return new Account(UUID.fromString(r.getString(1)),r.getString(2),r.getString(3),r.getString(4));}}
 public void create(UUID u,String n,String h,String s)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO accounts VALUES(?,?,?,?,?,NULL)")){p.setString(1,u.toString());p.setString(2,n);p.setString(3,h);p.setString(4,s);p.setLong(5,System.currentTimeMillis());p.executeUpdate();}}
 public void login(UUID u)throws SQLException{try(PreparedStatement p=c.prepareStatement("UPDATE accounts SET last_login=? WHERE uuid=?")){p.setLong(1,System.currentTimeMillis());p.setString(2,u.toString());p.executeUpdate();}}
 public void delete(UUID u)throws SQLException{try(PreparedStatement p=c.prepareStatement("DELETE FROM accounts WHERE uuid=?")){p.setString(1,u.toString());p.executeUpdate();}sessionDelete(u);}
 public void password(UUID u,String h,String s)throws SQLException{try(PreparedStatement p=c.prepareStatement("UPDATE accounts SET password_hash=?,salt=? WHERE uuid=?")){p.setString(1,h);p.setString(2,s);p.setString(3,u.toString());p.executeUpdate();}}
 public Session session(UUID u)throws SQLException{try(PreparedStatement p=c.prepareStatement("SELECT ip,expires_at FROM sessions WHERE uuid=?")){p.setString(1,u.toString());var r=p.executeQuery();return r.next()?new Session(r.getString(1),r.getLong(2)):null;}}
 public void sessionSave(UUID u,String ip,long ex)throws SQLException{try(PreparedStatement p=c.prepareStatement("INSERT INTO sessions VALUES(?,?,?) ON CONFLICT(uuid) DO UPDATE SET ip=excluded.ip,expires_at=excluded.expires_at")){p.setString(1,u.toString());p.setString(2,ip);p.setLong(3,ex);p.executeUpdate();}}
 public void sessionDelete(UUID u)throws SQLException{try(PreparedStatement p=c.prepareStatement("DELETE FROM sessions WHERE uuid=?")){p.setString(1,u.toString());p.executeUpdate();}}
 public void close(){try{c.close();}catch(Exception ignored){}}
 public record Account(UUID uuid,String name,String hash,String salt){} public record Session(String ip,long expiresAt){}
}
