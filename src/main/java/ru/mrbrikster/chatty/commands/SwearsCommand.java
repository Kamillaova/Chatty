package ru.mrbrikster.chatty.commands;

import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import org.bukkit.command.CommandSender;
import ru.mrbrikster.chatty.util.BukkitCommand;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.moderation.SwearModerationMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SwearsCommand extends BukkitCommand {
  SwearsCommand() {
    super("swears", "swear");
  }

  @Override
  public void handle(CommandSender sender, String label, String[] args) {
    if (sender.hasPermission("chatty.command.swears")) {
      if (args.length == 2 &&
        args[0].equalsIgnoreCase("add")
      ) {
        var word = args[1];

        if (SwearModerationMethod.addWhitelistWord(word)) {
          try {
            Files.asCharSink(
              SwearModerationMethod.getWhitelistFile(),
              StandardCharsets.UTF_8,
              FileWriteMode.APPEND
            ).write("\n" + word);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

        sender.sendMessage(Chatty.instance().messages().get("swears-command.add-word").replace("{word}", word));
      } else {
        sender.sendMessage(Chatty.instance().messages().get("swears-command.usage")
          .replace("{label}", label)
        );
      }
    } else {
      sender.sendMessage(Chatty.instance().messages().get("no-permission"));
    }
  }
}
