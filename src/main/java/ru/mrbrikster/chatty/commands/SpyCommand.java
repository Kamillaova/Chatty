package ru.mrbrikster.chatty.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.util.BukkitCommand;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.JsonStorage;
import ru.mrbrikster.chatty.util.JsonPrimitives;

public class SpyCommand extends BukkitCommand {
  private final JsonStorage jsonStorage;

  SpyCommand(JsonStorage jsonStorage) {
    super("spy");

    this.jsonStorage = jsonStorage;
  }

  @Override
  public void handle(CommandSender sender, String label, String[] args) {
    if (sender instanceof Player) {
      if (!sender.hasPermission("chatty.command.spy")) {
        sender.sendMessage(Chatty.instance().messages().get("no-permission"));
        return;
      }

      if (jsonStorage.getProperty((Player) sender, "spy-mode").orElse(JsonPrimitives.TRUE).getAsBoolean()) {
        jsonStorage.setProperty((Player) sender, "spy-mode", JsonPrimitives.FALSE);
        sender.sendMessage(Chatty.instance().messages().get("spy-off"));
      } else {
        jsonStorage.setProperty((Player) sender, "spy-mode", JsonPrimitives.TRUE);
        sender.sendMessage(Chatty.instance().messages().get("spy-on"));
      }
    } else {
      sender.sendMessage(Chatty.instance().messages().get("only-for-players"));
    }
  }
}
