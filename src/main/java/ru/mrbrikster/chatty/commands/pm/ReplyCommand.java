package ru.mrbrikster.chatty.commands.pm;

import com.google.gson.JsonElement;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.util.ArrayWrapper;

import java.util.logging.Level;

public class ReplyCommand extends PrivateMessageCommand {
  public ReplyCommand(Chatty chatty) {
    super(
      chatty,
      "r",
      ArrayWrapper.toArray(
        chatty.get(Configuration.class)
          .getNode("pm.commands.reply.aliases").
          getAsStringList()
          .stream().map(alias -> {
            if (alias.equalsIgnoreCase("r")) {
              chatty.getLogger().log(Level.WARNING, "Please, rename \"r\" alias to \"reply\" in reply command configuration. " +
                "This change was made due to EssentialsX with default command name \"r\" instead of \"reply\"");

              return "reply";
            }

            return alias;
          }).toList(),
        String.class
      )
    );
  }

  @Override
  public void handle(CommandSender sender, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage(Chatty.instance().messages().get("only-for-players"));
      return;
    }

    if (!sender.hasPermission("chatty.command.reply")) {
      sender.sendMessage(Chatty.instance().messages().get("no-permission"));
      return;
    }

    if (args.length < 1) {
      sender.sendMessage(Chatty.instance().messages().get("reply-command.usage")
        .replace("{label}", label)
      );
      return;
    }

    var message = String.join(" ", args);

    var optionalRecipient = jsonStorage.getProperty(player, "last-pm-interlocutor").map(JsonElement::getAsString);
    if (optionalRecipient.isEmpty()) {
      sender.sendMessage(Chatty.instance().messages().get("reply-command.target-not-found"));
      return;
    }

    CommandSender recipient = optionalRecipient.get().equalsIgnoreCase("CONSOLE") &&
                                configuration.getNode("pm.allow-console").getAsBoolean(false)
                                  ? Bukkit.getConsoleSender()
                                  : Bukkit.getPlayer(optionalRecipient.get());

    if (recipient == null || isOffline(sender, recipient)) {
      sender.sendMessage(Chatty.instance().messages().get("reply-command.target-not-found"));
      return;
    }

    if (recipient instanceof Player recipientPlayer
      && !configuration.getNode("pm.allow-pm-vanished").getAsBoolean(true)
      && !player.canSee(recipientPlayer)
    ) {
      sender.sendMessage(Chatty.instance().messages().get("reply-command.target-not-found"));
      return;
    }

    handlePrivateMessage(sender, recipient, message);
  }
}
