package ru.mrbrikster.chatty.commands.pm;

import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.JsonStorage;
import ru.mrbrikster.chatty.dependencies.PlayerTagManager;
import ru.mrbrikster.chatty.json.FormattedMessage;
import ru.mrbrikster.chatty.moderation.ModerationManager;
import ru.mrbrikster.chatty.util.JsonPrimitives;
import ru.mrbrikster.chatty.util.Sound;
import ru.mrbrikster.chatty.util.TextUtil;

import static ru.mrbrikster.chatty.util.TextUtil.stylish;

public abstract class PrivateMessageCommand extends BukkitCommand {
  private static final String MODERATION_COLOR_SYMBOL = "§z";

  protected final Configuration configuration;
  protected final JsonStorage jsonStorage;

  private final PlayerTagManager playerTagManager;
  private final ModerationManager moderationManager;

  public PrivateMessageCommand(Chatty chatty, String name, String... aliases) {
    super(name, aliases);

    this.configuration = chatty.get(Configuration.class);
    this.jsonStorage = chatty.get(JsonStorage.class);

    this.playerTagManager = chatty.get(PlayerTagManager.class);
    this.moderationManager = chatty.get(ModerationManager.class);
  }

  protected void handlePrivateMessage( CommandSender sender,  CommandSender recipient,  String message) {
    var recipientName = recipient.getName();
    var recipientPrefix = "";
    var recipientSuffix = "";

    if (recipient instanceof Player) {
      recipientName = ((Player) recipient).getDisplayName();
      recipientPrefix = playerTagManager.getPrefix((Player) recipient);
      recipientSuffix = playerTagManager.getSuffix((Player) recipient);
      jsonStorage.setProperty((Player) recipient, "last-pm-interlocutor", new JsonPrimitive(sender.getName()));
    }

    var cancelledByModeration = false;

    var senderName = sender.getName();
    var senderPrefix = "";
    var senderSuffix = "";

    if (sender instanceof Player) {
      senderName = ((Player) sender).getDisplayName();
      senderPrefix = playerTagManager.getPrefix((Player) sender);
      senderSuffix = playerTagManager.getSuffix((Player) sender);
      jsonStorage.setProperty((Player) sender, "last-pm-interlocutor", new JsonPrimitive(recipientName));

      if (moderationManager.isSwearModerationEnabled()) {
        var swearMethod = moderationManager.getSwearMethod(message, MODERATION_COLOR_SYMBOL);
        if (!sender.hasPermission("chatty.moderation.swear")) {
          if (swearMethod.isBlocked()) {
            message = swearMethod.getEditedMessage();

            if (swearMethod.isUseBlock()) {
              cancelledByModeration = true;
            } else {
              message = swearMethod.getEditedMessage();
            }

            var swearFound = Chatty.instance().messages().get("swear-found", null);

            if (swearFound != null) {
              Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(),
                () -> sender.sendMessage(swearFound),
                5L
              );
            }
          }
        }
      }

      if (this.moderationManager.isAdvertisementModerationEnabled()) {
        var advertisementMethod = this.moderationManager.getAdvertisementMethod(message, MODERATION_COLOR_SYMBOL);
        if (!sender.hasPermission("chatty.moderation.advertisement")) {
          if (advertisementMethod.isBlocked()) {
            message = advertisementMethod.getEditedMessage();

            if (advertisementMethod.isUseBlock()) {
              cancelledByModeration = true;
            } else {
              message = advertisementMethod.getEditedMessage();
            }

            var adsFound = Chatty.instance().messages().get("advertisement-found", null);

            if (adsFound != null) {
              Bukkit.getScheduler().runTaskLaterAsynchronously(Chatty.instance(),
                () -> sender.sendMessage(adsFound), 5L
              );
            }
          }
        }
      }
    }

    if (cancelledByModeration) {
      return;
    }

    String senderFormat;
    if (!jsonStorage.isIgnore(recipient, sender)) {
      var recipientFormat = createFormat(configuration.getNode("pm.format.recipient")
          .getAsString("&7{sender-prefix}{sender-name} &6-> &7{recipient-prefix}{recipient-name}: &f{message}"),
        message, recipientName, recipientPrefix, recipientSuffix,
        senderName, senderPrefix, senderSuffix
      );

      if (!(recipient instanceof Player)) {
        recipientFormat = TextUtil.stripHex(recipientFormat);
        recipient.sendMessage(recipientFormat);
      } else {
        var formattedMessage = new FormattedMessage(recipientFormat);
        formattedMessage.toFancyMessage().send((Player) recipient, (Player) sender);

        var soundName = configuration.getNode("pm.sound").getAsString(null);
        if (soundName != null) {
          var sound = Sound.byName(soundName);
          var soundVolume = (double) configuration.getNode("pm.sound-volume").get(1d);
          var soundPitch = (double) configuration.getNode("pm.sound-pitch").get(1d);
          ((Player) recipient).playSound(((Player) recipient).getLocation(), sound, (float) soundVolume, (float) soundPitch);
        }
      }
    }

    senderFormat = createFormat(configuration.getNode("pm.format.sender")
        .getAsString("&7{sender-prefix}{sender-name} &6-> &7{recipient-prefix}{recipient-name}: &f{message}"),
      message, recipientName, recipientPrefix, recipientSuffix,
      senderName, senderPrefix, senderSuffix
    );

    if (!(sender instanceof Player)) {
      TextUtil.stripHex(senderFormat);
    }

    sender.sendMessage(senderFormat);

    if (configuration.getNode("spy.enable").getAsBoolean(false)) {
      var stylishedSpyMessage = createFormat(configuration.getNode("spy.format.pm")
          .getAsString("&6[Spy] &r{format}"),
        message, recipientName, recipientPrefix, recipientSuffix,
        senderName, senderPrefix, senderSuffix
      )
        .replace("{format}", senderFormat);

      Bukkit.getOnlinePlayers().stream()
        .filter(spyPlayer -> !spyPlayer.equals(sender) && !spyPlayer.equals(recipient))
        .filter(spyPlayer -> spyPlayer.hasPermission("chatty.spy") || spyPlayer.hasPermission("chatty.spy.pm"))
        .filter(spyPlayer -> jsonStorage.getProperty(spyPlayer, "spy-mode")
          .orElse(JsonPrimitives.TRUE)
          .getAsBoolean()
        )
        .forEach(spyPlayer -> spyPlayer.sendMessage(stylishedSpyMessage));
    }
  }

  private String createFormat(
    String format,
    String message,
    String recipientName,
    String recipientPrefix,
    String recipientSuffix,
    String senderName,
    String senderPrefix,
    String senderSuffix
  ) {
    format = stylish(format
      .replace("{sender-prefix}", senderPrefix)
      .replace("{sender-suffix}", senderSuffix)
      .replace("{sender-name}", senderName)
      .replace("{recipient-name}", recipientName)
      .replace("{recipient-prefix}", recipientPrefix)
      .replace("{recipient-suffix}", recipientSuffix)
    );

    return format.replace("{message}", message.replace(MODERATION_COLOR_SYMBOL, getLastColors(format)));
  }

  private String getLastColors(String format) {
    var messageIndex = format.lastIndexOf("{message}");

    if (messageIndex == -1) {
      return format;
    }

    return TextUtil.getLastColors(stylish(format.substring(0, messageIndex)));
  }
}
