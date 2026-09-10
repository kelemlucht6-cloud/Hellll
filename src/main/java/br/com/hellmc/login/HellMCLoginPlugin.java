package br.com.hellmc.login;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;import org.bukkit.command.*;import org.bukkit.entity.Player;import org.bukkit.event.*;import org.bukkit.event.player.*;import org.bukkit.event.block.*;import org.bukkit.event.inventory.InventoryClickEvent;import org.bukkit.event.entity.EntityDamageByEntityEvent;import org.bukkit.plugin.java.JavaPlugin;
import java.net.InetSocketAddress;import java.sql.SQLException;import java.util.*;import java.util.concurrent.ConcurrentHashMap;
public final class HellMCLoginPlugin extends JavaPlugin implements Listener,CommandExecutor,TabCompleter{
 Database db; final Map<UUID,S> states=new ConcurrentHashMap<>();
 public void onEnable(){saveDefaultConfig();try{db=new Database(new java.io.File(getDataFolder(),"players.db"));}catch(Exception e){getLogger().severe("SQLite: "+e);Bukkit.getPluginManager().disablePlugin(this);return;}for(String n:new String[]{"register","login","logout","helllogin"}){var c=getCommand(n);if(c!=null){c.setExecutor(this);c.setTabCompleter(this);}}Bukkit.getPluginManager().registerEvents(this,this);}
 public void onDisable(){if(db!=null)db.close();}
 S s(Player p){return states.computeIfAbsent(p.getUniqueId(),x->new S());}
 boolean auth(Player p){return p.hasPermission("hellmclogin.admin")||s(p).ok;}
 String ip(Player p){InetSocketAddress a=p.getAddress();return a==null?"":a.getAddress().getHostAddress();}
 String color(String x){return (x==null?"":x).replace('&','§');}
 void msg(Player p,String k){p.sendMessage(Component.text(color(getConfig().getString("messages.prefix","")+getConfig().getString("messages."+k,""))));}
 void prepare(Player p){S x=s(p);x.ok=false;try{
   if(Bukkit.getOnlineMode()&&getConfig().getBoolean("settings.premium-auto-login",true)){x.ok=true;if(db.has(p.getUniqueId()))db.login(p.getUniqueId());msg(p,"login");return;}
   Database.Account a=db.get(p.getUniqueId()); if(a==null)msg(p,"need-register");else msg(p,"need-login");
   Bukkit.getScheduler().runTaskLater(this,()->{if(p.isOnline()&&!auth(p))p.kick(Component.text("Autenticacao necessaria."));},getConfig().getLong("settings.login-timeout-seconds",60)*20L);
 }catch(Exception e){p.kick(Component.text("Erro no sistema de login."));}}
 @EventHandler public void join(PlayerJoinEvent e){prepare(e.getPlayer());}
 @EventHandler public void quit(PlayerQuitEvent e){states.remove(e.getPlayer().getUniqueId());}
 public boolean onCommand(CommandSender z,Command c,String l,String[] a){
   if(c.getName().equalsIgnoreCase("helllogin"))return admin(z,a);
   if(!(z instanceof Player p))return true;
   if(c.getName().equalsIgnoreCase("register")){if(a.length!=2){msg(p,"usage-register");return true;}try{if(db.has(p.getUniqueId())){msg(p,"already-auth");return true;}int min=getConfig().getInt("settings.min-password-length",6);if(a[0].length()<min){p.sendMessage(color(getConfig().getString("messages.password-short","")).replace("{min}",""+min));return true;}if(!a[0].equals(a[1])){msg(p,"password-mismatch");return true;}String salt=PasswordUtil.salt();db.create(p.getUniqueId(),p.getName(),PasswordUtil.hash(a[0],salt),salt);s(p).ok=true;msg(p,"registered");}catch(Exception e){p.sendMessage("Erro interno.");}return true;}
   if(c.getName().equalsIgnoreCase("login")){if(a.length!=1){msg(p,"usage-login");return true;}try{var ac=db.get(p.getUniqueId());if(ac==null){msg(p,"need-register");return true;}if(!PasswordUtil.verify(a[0],ac.salt(),ac.hash())){s(p).fails++;msg(p,"wrong-password");return true;}s(p).ok=true;db.login(p.getUniqueId());msg(p,"login");}catch(Exception e){p.sendMessage("Erro interno.");}return true;}
   if(c.getName().equalsIgnoreCase("logout")){s(p).ok=false;msg(p,"logout");return true;}return true;
 }
 boolean admin(CommandSender z,String[] a){if(!z.hasPermission("hellmclogin.admin")){z.sendMessage("Sem permissao.");return true;}if(a.length==0||a[0].equalsIgnoreCase("info")){z.sendMessage("HellMCLogin 1.0.0 | online-mode="+Bukkit.getOnlineMode());return true;}if(a[0].equalsIgnoreCase("reload")){reloadConfig();z.sendMessage("Recarregado.");return true;}if(a[0].equalsIgnoreCase("unregister")&&a.length>1){Player p=Bukkit.getPlayerExact(a[1]);if(p!=null)try{db.delete(p.getUniqueId());z.sendMessage("Conta removida.");}catch(Exception e){z.sendMessage("Erro.");}return true;}return true;}
 boolean block(Player p){return !auth(p);}
 @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void move(PlayerMoveEvent e){if(block(e.getPlayer())&&getConfig().getBoolean("security.block-movement",true)&&e.getTo()!=null)e.setTo(e.getFrom());}
 @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void chat(AsyncPlayerChatEvent e){if(block(e.getPlayer()))e.setCancelled(true);}
 @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void interact(PlayerInteractEvent e){if(block(e.getPlayer()))e.setCancelled(true);}
 @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void br(BlockBreakEvent e){if(block(e.getPlayer()))e.setCancelled(true);}
 @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void bp(BlockPlaceEvent e){if(block(e.getPlayer()))e.setCancelled(true);}
 @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void inv(InventoryClickEvent e){if(e.getWhoClicked() instanceof Player p&&block(p))e.setCancelled(true);}
 @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)public void dmg(EntityDamageByEntityEvent e){if(e.getDamager() instanceof Player p&&block(p))e.setCancelled(true);if(e.getEntity() instanceof Player p&&block(p))e.setCancelled(true);}
 public List<String> onTabComplete(CommandSender s,Command c,String l,String[] a){return Collections.emptyList();}
 static class S{boolean ok;int fails;}
}
