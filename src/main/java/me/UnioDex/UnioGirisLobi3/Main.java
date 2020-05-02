package me.UnioDex.UnioGirisLobi3;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.connorlinfoot.titleapi.TitleAPI;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Main extends JavaPlugin implements Listener {
    public Set<String> invisiblePlayers = new HashSet<String>();
    static FileConfiguration fc;
    static SQLManager sql;
    public static Plugin plugin;

    public void onEnable() {
        fc = this.getConfig();
        getServer().getScheduler().runTaskTimer(this, new RealTimeTask(this), 0L, 60);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        plugin = this;
        initDatabase();
        final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(this, PacketType.Play.Client.TAB_COMPLETE) {
            public void onPacketReceiving(PacketEvent event) {
                if ((event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) &&
                        (event.getPacket().getStrings().read(0).startsWith("/")) && (event.getPacket().getStrings().read(0).split(" ").length == 1)) {
                    event.setCancelled(true);
                }
            }
        });

        Bukkit.getServer().getScheduler()
                .scheduleAsyncDelayedTask(this, () -> {
                    long time = new Date().getTime() - 86400000L * 60;

                    File folder = new File(Bukkit.getServer().getWorldContainer().getAbsolutePath() + "/logs");
                    if (!folder.exists()) {
                        return;
                    }
                    File[] files = folder.listFiles();

                    @SuppressWarnings("unused")
                    int deleted = 0;
                    for (File file : files) {
                        if ((file.isFile()) && (file.getName().endsWith(".log.gz")) && (time > parseTime(file.getName().replace(".log.gz", "")).getTime())) {
                            file.delete();
                            deleted++;
                        }
                    }
                    System.out.println("Loglar temizlendi.");
                }, 1L);
    }

    public static Date parseTime(String time) {
        try {
            String[] frag = time.split("-");
            if (frag.length < 2) {
                return new Date();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.parse(frag[0] + "-" + frag[1] + "-" + frag[2]);
        } catch (Exception ignored) {
        }
        return new Date();
    }

    public void onDisable() {
        sql.onDisable();
    }

    private void initDatabase() {
        sql = new SQLManager(this); // Veritabanı bağlantısını hazırla
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {

            String pname = event.getPlayer().getName();

            if (!sql.playerExist(pname)) {
                String rejoinMessage = "\n§cOyuna girebilmek için üye olmalısınız! \n§cLütfen www.uniocraft.com adresinden üye olunuz.";
                event.setKickMessage(rejoinMessage);
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, rejoinMessage);
                return;
            }

            if (!sql.isPlayerActive(pname)) {
                String rejoinMessage = "\n§cOyuna girebilmek için hesabınız onaylı olmalıdır! \n§cLütfen email adresinize gelen onaylama linkine tıklayınız.";
                event.setKickMessage(rejoinMessage);
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, rejoinMessage);
                return;
            }

            String name = sql.getPlayerName(event.getPlayer().getName());
            String username = event.getPlayer().getName();
            if (!name.equals(username)) {
                String Kickmessage = "\n§cİsminiz büyük-küçük harf duyarlıdır. \n§cLütfen büyük-küçük harflere dikkat ederek §e" + name + " §cismiyle tekrar giriş yapın!";
                event.setKickMessage(Kickmessage);
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, Kickmessage);
                return;
            }

        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (int i = 0; i < 20; i++) {
            event.getPlayer().sendMessage("");
        }
        TitleAPI.sendTitle(event.getPlayer(), 20, 250, 20, getConfig().getString("UstBaslik").replaceAll("&", "§"), getConfig().getString("AltBaslik").replaceAll("&", "§"));
    }

    @EventHandler
    public void loginyaptiginda(LoginEvent event) {
        TitleAPI.sendTitle(event.getPlayer(), 20, 20, 20, getConfig().getString("GirisSonrasiUstBaslik").replaceAll("&", "§"), getConfig().getString("GirisSonrasiAltBaslik").replaceAll("&", "§"));
        event.getPlayer().sendMessage("§2§l*§1§l-§2§l*§1§l-§2§l*§1§l-§2§l*---§b§lSunucuya giriş yapılıyor§2§l---§2§l*§1§l-§2§l*§1§l-§2§l*§1§l-§2§l*");
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(this, () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(getConfig().getString("serverAfterLogin"));
            event.getPlayer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        }, 40L);
    }

    @EventHandler
    public void chateventi(PlayerChatEvent event) {
        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void HideJoin(PlayerJoinEvent event) {
        //on player join
        //YENİYERALTTA
        Player p = event.getPlayer();
        if (!invisiblePlayers.contains(p.getName())) {
            invisiblePlayers.add(p.getName());
            for (String s : invisiblePlayers) {
                if (Bukkit.getPlayer(s) != null) {
                    Player ply = Bukkit.getPlayer(p.getName());
                    Player online = Bukkit.getPlayer(s);

                    //make the joined player invisible
                    online.hidePlayer(ply);
                    ply.hidePlayer(online);
                }
            }
        } else {
            for (String s : invisiblePlayers) {
                if (Bukkit.getPlayer(s) != null) {
                    Player ply = Bukkit.getPlayer(p.getName());
                    Player online = Bukkit.getPlayer(s);

                    //make the joined player invisible
                    online.hidePlayer(ply);
                    ply.hidePlayer(online);
                }
            }
        }
        event.setJoinMessage(null);
    }

    @EventHandler
    public void Atilinca(PlayerKickEvent event) {
        event.setLeaveMessage("");
    }

    @EventHandler
    public void HideQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChatTabCompleteEvent(PlayerChatTabCompleteEvent event) {
        event.getTabCompletions().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void SignChangeOzellik(SignChangeEvent event) {
        // Tabelaya renkli yazı yazılabilmesini sağlar
        String CHAR = "&";
        char[] ch = CHAR.toCharArray();
        for (int i = 0; i <= 3; i++) {
            String line = event.getLine(i);
            line = ChatColor.translateAlternateColorCodes(ch[0], line);
            event.setLine(i, line);
        }
    }

    @EventHandler
    public void KomuttEventi(PlayerCommandPreprocessEvent evt) {
        Player player = evt.getPlayer();
        String[] cmd = evt.getMessage().replaceFirst("/", "").split(" ");
        if (cmd[0].equalsIgnoreCase("login") || cmd[0].equalsIgnoreCase("server") || (cmd[0].equalsIgnoreCase("l")) || (cmd[0].equalsIgnoreCase("gonder")) || (cmd[0].equalsIgnoreCase("baslangic")) || (cmd[0].equalsIgnoreCase("lobi")) || (cmd[0].equalsIgnoreCase("kitpvp")) || (cmd[0].equalsIgnoreCase("hackpvp")) || (cmd[0].equalsIgnoreCase("factions")) || (cmd[0].equalsIgnoreCase("hg")) || (cmd[0].equalsIgnoreCase("hungergames")) || (cmd[0].equalsIgnoreCase("sg")) || (cmd[0].equalsIgnoreCase("survivalgames")) || (cmd[0].equalsIgnoreCase("oitc")) || (cmd[0].equalsIgnoreCase("skyblock2")) || (cmd[0].equalsIgnoreCase("creative")) || (cmd[0].equalsIgnoreCase("skyblock")) || (cmd[0].equalsIgnoreCase("survival")) || (cmd[0].equalsIgnoreCase("skywars")) || (cmd[0].equalsIgnoreCase("superwars")) || (cmd[0].equalsIgnoreCase("parkur")) || (cmd[0].equalsIgnoreCase("asitadasi")) || (cmd[0].equalsIgnoreCase("giris"))) {
            return;
        } else {
            evt.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Bilinmeyen bir komut girdiniz. Kullanabileceğiniz komutlar: " + ChatColor.GREEN + "/login, /l");
        }

        if (cmd[0].equalsIgnoreCase("giris")) {
            if (!cmd[1].isEmpty()) {
                player.performCommand("login " + cmd[1]);
            }
        }
    }
}	
