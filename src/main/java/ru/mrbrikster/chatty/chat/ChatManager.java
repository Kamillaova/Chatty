package ru.mrbrikster.chatty.chat;

import com.google.gson.JsonPrimitive;
import lombok.Getter;
import ru.mrbrikster.chatty.util.ArrayWrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.event.ChattyAsyncPlayerChatEvent;
import ru.mrbrikster.chatty.util.Sound;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class ChatManager {
  private static final Pattern CHAT_NAME_PATTERN = Pattern.compile("^[a-z0-9]{1,32}$");
  @Getter
  private final List<Chat> chats = new ArrayList<>();
  @Getter
  private final Logger logger;
  private final Configuration configuration;
  private final JsonStorage jsonStorage;

  public ChatManager(Chatty chatty) {
    this.configuration = chatty.get(Configuration.class);
    this.jsonStorage = chatty.get(JsonStorage.class);
    this.logger = new Logger();

    init();

    configuration.onReload(config -> reload());
  }

  public Chat getChat(String chatName) {
    for (var chat : chats) {
      if (chat.getName().equalsIgnoreCase(chatName)) {
        return chat;
      }
    }

    return null;
  }

  public Chat getCurrentChat(Player player) {
    var optional = jsonStorage.getProperty(player, "chat");
    if (optional.isPresent()) {
      var jsonElement = optional.get();
      if (jsonElement.isJsonPrimitive()) {
        var chatName = jsonElement.getAsJsonPrimitive().getAsString();
        var chat = getChat(chatName);
        if (chat != null && chat.isWriteAllowed(player)) {
          return chat;
        }
      }
    }
    return null;
  }

  private void init() {
    configuration.getNode("chats").getChildNodes().stream().map(chatNode -> {
      var builder = Chat.builder()
        .name(chatNode.getName().toLowerCase())
        .displayName(chatNode.getNode("display-name").getAsString(chatNode.getName().toLowerCase()))
        .enable(chatNode.getNode("enable").getAsBoolean(false))
        .format(chatNode.getNode("format").getAsString("§7{player}§8: §f{message}"))
        .range(chatNode.getNode("range").getAsInt(-1))
        .symbol(chatNode.getNode("symbol").getAsString(""))
        .permissionRequired(chatNode.getNode("permission").getAsBoolean(true))
        .cooldown(chatNode.getNode("cooldown").getAsLong(-1))
        .money(chatNode.getNode("money").getAsInt(0));

      var chatCommand = chatNode.getNode("command").getAsString(null);
      if (chatCommand != null) {
        builder.command(chatCommand)
          .aliases(chatNode.getNode("aliases").getAsStringList());
      }

      var soundName = chatNode.getNode("sound").getAsString(null);

      if (soundName != null) {
        builder.sound(Sound.byName(soundName));
      }

      var moderationNode = chatNode.getNode("moderation");
      builder.capsModerationEnabled(moderationNode.getNode("caps").getAsBoolean(true))
        .swearModerationEnabled(moderationNode.getNode("swear").getAsBoolean(true))
        .advertisementModerationEnabled(moderationNode.getNode("advertisement").getAsBoolean(true));

      builder.spyEnabled(chatNode.getNode("spy").getAsBoolean(true));

      return builder.build();
    }).filter(Chat::isEnable).peek(chat -> {
      if (!CHAT_NAME_PATTERN.matcher(chat.getName()).matches()) {
        throw new IllegalArgumentException("Chat name can contain only Latin characters and numbers");
      }
    }).forEach(this.chats::add);

    for (var chat : this.chats) {
      if (chat.getCommand() != null) {
        chat.setBukkitCommand(new BukkitCommand(chat.getCommand(), ArrayWrapper.toArray(chat.getAliases(), String.class)) {
          @Override
          public void handle(CommandSender sender, String label, String[] args) {
            if (sender instanceof Player) {
              if (!sender.hasPermission("chatty.command.chat")) {
                sender.sendMessage(Chatty.instance().messages().get("no-permission"));
                return;
              }

              if (chat.isWriteAllowed((Player) sender)) {
                if (args.length == 0) {
                  jsonStorage.setProperty((Player) sender, "chat", new JsonPrimitive(chat.getName()));
                  sender.sendMessage(Chatty.instance().messages().get("chat-command.chat-switched")
                    .replace("{chat}", chat.getDisplayName())
                  );
                } else {
                  Bukkit.getScheduler().runTaskAsynchronously(Chatty.instance(), () -> {
                    AsyncPlayerChatEvent event = new ChattyAsyncPlayerChatEvent(
                      (Player) sender,
                      String.join(" ", args),
                      chat
                    );

                    Bukkit.getPluginManager().callEvent(event);

                    if (!event.isCancelled()) {
                      for (var recipient : event.getRecipients()) {
                        recipient.sendMessage(String.format(
                          event.getFormat(),
                          event.getPlayer().getDisplayName(),
                          event.getMessage()
                        ));
                      }
                    }
                  });
                }
              } else {
                sender.sendMessage(Chatty.instance().messages().get("chat-command.no-chat-permission"));
              }
            } else {
              sender.sendMessage(Chatty.instance().messages().get("only-for-players"));
            }
          }
        });

        chat.getBukkitCommand().register(Chatty.instance());
      }
    }
  }

  private void reload() {
    chats.forEach(chat -> {
      if (chat.getBukkitCommand() != null) {
        chat.getBukkitCommand().unregister(Chatty.instance());
      }
    });

    chats.clear();
    init();
  }

  public static class Logger {

    void write(Player player, String message, String additionalPrefix) {
      BufferedWriter bufferedWriter = null;
      var logsDirectory = new File(Chatty.instance().getDataFolder().getAbsolutePath() + File.separator + "logs");

      if (!logsDirectory.exists() && !logsDirectory.mkdir()) {
        return;
      }

      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      var calendar = Calendar.getInstance();
      var fileName = String.format("%s.log", dateFormat.format(calendar.getTime()));

      dateFormat = new SimpleDateFormat("[HH:mm:ss] ");
      var prefix = dateFormat.format(calendar.getTime());
      var line = String.format("%1$s%2$s%3$s (%4$s): %5$s",
        prefix, additionalPrefix, player.getName(), player.getUniqueId(), message
      );

      try {
        bufferedWriter = new BufferedWriter(new FileWriter(logsDirectory + File.separator + fileName, true));
        bufferedWriter.write(line);
        bufferedWriter.newLine();
      } catch (IOException ignored) {

      } finally {
        try {
          if (bufferedWriter != null) {
            bufferedWriter.flush();
            bufferedWriter.close();
          }
        } catch (Exception ignored) { }
      }
    }
  }
}
