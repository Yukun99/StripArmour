package me.yukun.striparmour.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import me.yukun.striparmour.StripArmour;
import me.yukun.striparmour.config.Config;
import me.yukun.striparmour.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StripCommand extends StripArmourCommand {

  private Player victim = null;
  private final List<Function<Player, Boolean>> strippers = new ArrayList<>();
  private static final Map<String, Function<Player, Boolean>> stripCommandMap = new HashMap<>() {{
    put("helmet", (player) -> {
      ItemStack helmet = player.getInventory().getHelmet();
      player.getInventory().setHelmet(null);
      if (!tryStripping(player, helmet)) {
        player.getInventory().setHelmet(helmet);
        return false;
      }
      return true;
    });
    put("chestplate", (player) -> {
      ItemStack chestplate = player.getInventory().getChestplate();
      player.getInventory().setChestplate(null);
      if (!tryStripping(player, chestplate)) {
        player.getInventory().setChestplate(chestplate);
        return false;
      }
      return true;
    });
    put("leggings", (player) -> {
      ItemStack leggings = player.getInventory().getLeggings();
      player.getInventory().setLeggings(null);
      if (!tryStripping(player, leggings)) {
        player.getInventory().setLeggings(leggings);
        return false;
      }
      return true;
    });
    put("boots", (player) -> {
      ItemStack boots = player.getInventory().getBoots();
      player.getInventory().setBoots(null);
      if (!tryStripping(player, boots)) {
        player.getInventory().setBoots(boots);
        return false;
      }
      return true;
    });
    put("offhand", (player) -> {
      if (StripArmour.getVersion() < 190) {
        return true;
      }
      ItemStack offhand = player.getInventory().getItemInOffHand();
      player.getInventory().setItemInOffHand(null);
      if (!tryStripping(player, offhand)) {
        player.getInventory().setItemInOffHand(offhand);
        return false;
      }
      return true;
    });
  }};

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private static boolean tryStripping(Player player, ItemStack item) {
    if (!Config.doIgnoreNoSpace() && player.getInventory().firstEmpty() == -1) {
      return false;
    }
    if (item == null) {
      return true;
    }
    if (player.getInventory().firstEmpty() == -1) {
      player.getWorld().dropItemNaturally(player.getLocation(), item);
    } else {
      player.getInventory().addItem(item);
    }
    return true;
  }

  public StripCommand(CommandSender sender) {
    super(sender);
  }

  public void withArguments(String[] args) {
    Player player = Bukkit.getPlayer(args[1]);
    if (player == null) {
      return;
    }
    victim = player;
    if (args.length == 2) {
      strippers.addAll(stripCommandMap.values());
      return;
    }
    for (int i = 2; i < args.length; i++) {
      if (!stripCommandMap.containsKey(args[i])) {
        Messages.sendStripInvalidArmour(sender, args[i]);
        strippers.clear();
        break;
      }
      if (args[i].equals("offhand") && !Config.doStripOffHand()) {
        continue;
      }
      strippers.add(stripCommandMap.get(args[i]));
    }
  }

  @Override
  public boolean execute() {
    if (victim == null) {
      Messages.sendStripNoPlayer(sender);
      return false;
    }
    if (!Config.canStrip(victim)) {
      Messages.sendStripImmune(sender);
      return false;
    }
    boolean hasSpace = true;
    for (Function<Player, Boolean> stripper : strippers) {
      hasSpace = stripper.apply(victim) && hasSpace;
    }
    if (hasSpace) {
      Messages.sendStrip(sender, victim);
    } else {
      Messages.sendNoSpace(sender);
    }
    Messages.sendStripped(victim);
    return true;
  }
}
