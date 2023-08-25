package org.tenmillionapples.signedsigns;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

public class Events implements Listener {
    private final List<Player> shiftPlacedSign = new ArrayList<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignPlace(BlockPlaceEvent e) {
        FileConfiguration config = SignedSigns.getInstance().getConfig();
        if (!(e.getBlock().getState() instanceof Sign)
                || e.isCancelled()
                || !config.getBoolean("anonymous-signing")
                || !e.getPlayer().hasPermission("signedsigns.anonymoussigning")
                || !e.getPlayer().isSneaking()
                || e.getPlayer().hasPermission("signedsigns.nosigning")) {
            return;
        }

        shiftPlacedSign.add(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSignChange(SignChangeEvent e) {
        FileConfiguration config = SignedSigns.getInstance().getConfig();
        Player player = e.getPlayer();

        if (player.hasPermission("signedsigns.nosigning")) {
            return;
        }

        if (!config.getBoolean("sign-empty-signs")) {
            boolean empty = true;
            for (String line : e.getLines()) {
                if (!line.equals("")) {
                    empty = false;
                    break;
                }
            }

            if (empty)
                return;
        }

        // Get configuration
        List<String> date = config.getStringList("date-format");
        List<String> signature = config.getStringList("signature-format");
        String timezoneFormat = config.getString("time-zone");
        String formatOrder = config.getString("format-order");

        boolean anonymous = shiftPlacedSign.remove(player);

        // Get time and date
        Calendar calendar;
        if (timezoneFormat.equalsIgnoreCase("DEFAULT")){
            calendar = Calendar.getInstance();
        } else {
            calendar = Calendar.getInstance(TimeZone.getTimeZone(timezoneFormat));
        }

        // Format lines
        date.replaceAll(s -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat(s);
            dateFormat.setTimeZone(calendar.getTimeZone());
            return dateFormat.format(calendar.getTime());
        });

        signature.replaceAll(s -> {
            s = ChatColor.translateAlternateColorCodes('&', s);
            return s.replace("%player_name%",
                    anonymous ? config.getString("anonymous-signature") : player.getName());
        });

        List<String> lines = new ArrayList<>();
        if (formatOrder.equalsIgnoreCase("DATE-FIRST")) {
            lines.addAll(date);
            lines.addAll(signature);
        } else if (formatOrder.equalsIgnoreCase("SIGNATURE-FIRST")) {
            lines.addAll(signature);
            lines.addAll(date);
        } else {
            SignedSigns.getInstance().getLogger().log(Level.SEVERE, "Format order is an invalid type.");
            return;
        }

        int j = 0;
        for (int i = 4 - lines.size(); i < 4; i++) {
            e.setLine(i, lines.get(j));
            j++;
        }
    }
}
