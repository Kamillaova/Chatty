package ru.mrbrikster.chatty.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;
import ru.mrbrikster.baseplugin.plugin.BasePlugin;
import ru.mrbrikster.baseplugin.plugin.BukkitBasePlugin;

import java.lang.reflect.Field;
import java.util.Locale;

public abstract class BukkitCommand extends ru.mrbrikster.baseplugin.commands.BukkitCommand {
  private static final SimpleCommandMap commandMap;

  static {
    try {
      var simplePluginManager = (SimplePluginManager) Bukkit.getServer().getPluginManager();

      Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
      commandMapField.setAccessible(true);

      commandMap = (SimpleCommandMap) commandMapField.get(simplePluginManager);
    } catch (Throwable t) {
      throw new ExceptionInInitializerError(t);
    }
  }

  public BukkitCommand(String name, String... aliases) {
    super(name, aliases);
  }

  @Override
  public abstract void handle(CommandSender sender, String label, String[] args);

  @Override
  public void register(BasePlugin basePlugin) {
    if (!(basePlugin instanceof BukkitBasePlugin plugin)) throw new UnsupportedOperationException();

    var label = getName().toLowerCase(Locale.ENGLISH).trim();

    register(commandMap);
    commandMap.getKnownCommands().put(label, this);

    for (var alias : getAliases()) {
      commandMap.getKnownCommands().put(alias.toLowerCase(Locale.ENGLISH).trim(), this);
    }

    plugin.commands.add(this);
  }
}
