package me.UnioDex.UnioGirisLobi3;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLManager {
	private final ConnectionPoolManager pool;
 
	public SQLManager(Main plugin) {
		pool = new ConnectionPoolManager(plugin);
	}
	  	
	public void updateSQL(String QUERY)
	{
		try ( Connection connection = pool.getConnection() ) {
			PreparedStatement statement = connection.prepareStatement(QUERY);
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	  public int getUserID(String player)
	  {
		  String QUERY = "SELECT user_id FROM `website`.`xf_user` WHERE username = '" + player + "';";
		  try ( Connection connection = pool.getConnection() ) {
			  PreparedStatement statement = connection.prepareStatement(QUERY);
			  ResultSet res = statement.executeQuery();
			  if (res.next())
			  {
				  return res.getInt("user_id");
			  }else {
				  return 0;
			  }
		  } catch (SQLException e) {
			  e.printStackTrace();
		  }
		  return 0;
	  }
	
	  public boolean playerExist(String player)
	  {
		  String table = Main.fc.getString("database.table");
	      String username = Main.fc.getString("database.usernameColumn");
	      
		  String QUERY = "SELECT * FROM " + table + " WHERE " + username + " = '" + player + "';";
		  try ( Connection connection = pool.getConnection() ) {
			  PreparedStatement statement = connection.prepareStatement(QUERY);
			  ResultSet res = statement.executeQuery();
			  return res.next();
		  } catch (SQLException e) {
			  e.printStackTrace();
		  }
		  return false;
	  }
	  
	  public String getPlayerIP(String player)
	  {
		  int userid = getUserID(player); 
	      
		  String QUERY = "SELECT ip_id, user_id, content_type, content_id, action, log_date, INET_NTOA(CONV(HEX(ip), 16, 10)) AS ip FROM `website`.`xf_ip` WHERE user_id = "+userid+" ORDER BY ip_id DESC LIMIT 1";
		  try ( Connection connection = pool.getConnection() ) {
			  PreparedStatement statement = connection.prepareStatement(QUERY);
			  ResultSet res = statement.executeQuery();
			  if (res.next())
			  {
				  return res.getString("ip");
			  }else {
				  return "0";
			  }
		  } catch (SQLException e) {
			  e.printStackTrace();
		  }
		  return "0";
	  }
	  
	  public boolean checkPlayerIP(Player player)
	  {
		  String ip = getPlayerIP(player.getName());
		  return player.getAddress().getAddress().getHostAddress().equals(ip);
	  }
	  
	  public boolean isPlayerActive(String player)
	  {
		  String table = Main.fc.getString("database.table");
		  String username = Main.fc.getString("database.usernameColumn");
		  if (playerExist(player)) {
			  String QUERY = "SELECT user_state FROM " + table + " WHERE " + username + " = '" + player + "';";
			  try ( Connection connection = pool.getConnection() ) {
				  PreparedStatement statement = connection.prepareStatement(QUERY);
				  ResultSet res = statement.executeQuery();
				  if (res.next())
				  {
					  String userstate = res.getString("user_state");
					  return userstate.equals("valid");
				  }else {
					  return false;
				  }
			  } catch (SQLException e) {
				  e.printStackTrace();
			  }
	      }
		  return false;
	  }
	  
	  public String getPlayerName(String player)
	  {
	    String retval = null;
	    if (playerExist(player))
	    {
	    	String username = Main.fc.getString("database.usernameColumn");
	    	String table = Main.fc.getString("database.table");
	    	String QUERY = "SELECT " + username + " FROM " + table + " WHERE " + username + " = '" + player + "';";
	    	try ( Connection connection = pool.getConnection() ) {
	    		PreparedStatement statement = connection.prepareStatement(QUERY);
	    		ResultSet res = statement.executeQuery();
	    		if (res.next() && 
	        			(String.valueOf(res.getString(username)) != null)) {
	        		retval = String.valueOf(res.getString(username));
	    		}else {
	    			return retval;
	    		}
	    	} catch (SQLException e) {
	    		e.printStackTrace();
	    	}
	      }
	      else
	      {
	    	  return retval;
	      }
	      return retval;
	    }

	public void onDisable() {
		pool.closePool();
	}

}