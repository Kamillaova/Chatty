package ru.mrbrikster.chatty.commands;

import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import ru.mrbrikster.chatty.util.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.JsonStorage;

import java.util.Arrays;

import static ru.mrbrikster.chatty.util.TextUtil.stylish;

public class PrefixCommand extends BukkitCommand {
  private final Configuration configuration;
  private final JsonStorage jsonStorage;

  PrefixCommand(
    Configuration configuration,
    JsonStorage jsonStorage
  ) {
    super("prefix", "setprefix");

    this.configuration = configuration;
    this.jsonStorage = jsonStorage;
  }

  @Override
  public void handle(CommandSender sender, String label, String[] args) {
    if (args.length >= 2) {
      if (!sender.hasPermission("chatty.command.prefix")) {
        sender.sendMessage(Chatty.instance().messages().get("no-permission"));
        return;
      }

      var player = Bukkit.getPlayer(args[0]);

      if (player == null) {
        sender.sendMessage(Chatty.instance().messages().get("prefix-command.player-not-found"));
        return;
      }

      if (!player.equals(sender) && !sender.hasPermission("chatty.command.prefix.others")) {
        sender.sendMessage(Chatty.instance().messages().get("prefix-command.no-permission-others"));
        return;
      }

      if (args[1].equalsIgnoreCase("clear")) {
        jsonStorage.setProperty(player, "prefix", null);

        sender.sendMessage(Chatty.instance().messages().get("prefix-command.prefix-clear")
          .replace("{player}", player.getName())
        );
      } else {
        var prefix = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        var formattedPrefix = prefix + configuration.getNode("miscellaneous.commands.prefix.after-prefix").getAsString("");

        var minLimit = configuration.getNode("miscellaneous.commands.prefix.length-limit.min").getAsInt(3);
        var maxLimit = configuration.getNode("miscellaneous.commands.prefix.length-limit.max").getAsInt(16);
        if (formattedPrefix.length() > maxLimit) {
          sender.sendMessage(Chatty.instance().messages().get("prefix-command.length-limit-max")
            .replace(
              "{limit}",
              String.valueOf(maxLimit - formattedPrefix.length() + prefix.length())
            )
          );
          return;
        }

        if (formattedPrefix.length() < minLimit) {
          sender.sendMessage(Chatty.instance().messages().get("prefix-command.length-limit-min")
            .replace(
              "{limit}",
              String.valueOf(minLimit - formattedPrefix.length() + prefix.length())
            )
          );
          return;
        }

        jsonStorage.setProperty(player, "prefix", new JsonPrimitive(formattedPrefix));

        sender.sendMessage(Chatty.instance().messages().get("prefix-command.prefix-set")
          .replace("{player}", player.getName())
          .replace("{prefix}", stylish(prefix))
        );
      }
    } else {
      sender.sendMessage(Chatty.instance().messages().get("prefix-command.usage")
        .replace("{label}", label)
      );
    }
  }
}
