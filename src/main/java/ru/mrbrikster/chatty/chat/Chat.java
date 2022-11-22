package ru.mrbrikster.chatty.chat;

import com.google.gson.JsonArray;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.Nullable;
import ru.mrbrikster.baseplugin.commands.BukkitCommand;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.json.FormattedMessage;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Chat implements ru.mrbrikster.chatty.api.chats.Chat {
  private static final String CHAT_COOLDOWN_METADATA_KEY = "chatty.cooldown.chat.%s";

  private final String name;

  private final String displayName;
  private final boolean enable;

  private final String format;
  private final int range;

  private final String symbol;
  private final boolean permissionRequired;
  private final long cooldown;
  private final int money;
  @Nullable
  private final String command;
  @Nullable
  private final List<String> aliases;

  @Nullable
  private final Sound sound;

  private final boolean spyEnabled;

  private final boolean capsModerationEnabled;
  private final boolean swearModerationEnabled;
  private final boolean advertisementModerationEnabled;

  private BukkitCommand bukkitCommand;
  public Chat(String name, String displayName, boolean enable, String format, int range, String symbol, boolean permissionRequired, long cooldown, int money, @Nullable String command, @Nullable List<String> aliases, @Nullable Sound sound, boolean spyEnabled, boolean capsModerationEnabled, boolean swearModerationEnabled, boolean advertisementModerationEnabled, BukkitCommand bukkitCommand) {
    this.name = name;
    this.displayName = displayName;
    this.enable = enable;
    this.format = format;
    this.range = range;
    this.symbol = symbol;
    this.permissionRequired = permissionRequired;
    this.cooldown = cooldown;
    this.money = money;
    this.command = command;
    this.aliases = aliases;
    this.sound = sound;
    this.spyEnabled = spyEnabled;
    this.capsModerationEnabled = capsModerationEnabled;
    this.swearModerationEnabled = swearModerationEnabled;
    this.advertisementModerationEnabled = advertisementModerationEnabled;
    this.bukkitCommand = bukkitCommand;
  }
  public static ChatBuilder builder() { return new ChatBuilder(); }

  public boolean isWriteAllowed(Player player) {
    return !isPermissionRequired()
      // Не убирать (!)
      || player.hasPermission(String.format("chatty.chat.%s", getName()))
      || player.hasPermission(String.format("chatty.chat.%s.write", getName())
    );
  }

  void setCooldown(Player player) {
    player.setMetadata(
      String.format(CHAT_COOLDOWN_METADATA_KEY, name),
      new FixedMetadataValue(Chatty.instance(), System.currentTimeMillis())
    );
  }

  public long getCooldown(Player player) {
    var metadataValues = player.getMetadata(String.format(CHAT_COOLDOWN_METADATA_KEY, name));

    if (metadataValues.isEmpty()) { return -1; }

    var cooldown = (metadataValues.get(0).asLong() + (this.cooldown * 1000) - System.currentTimeMillis()) / 1000;
    return cooldown > 0 ? cooldown : -1;
  }

  @Override
  public boolean isPermissionRequired() {
    return permissionRequired;
  }

  @Override
  public Collection<? extends Player> getRecipients(@Nullable Player player) {
    return filterRecipients(player, new ArrayList<>(Bukkit.getOnlinePlayers()));
  }

  @Override

  public Collection<? extends Player> filterRecipients(@Nullable Player player, Collection<? extends Player> players) {
    if (range > -2 && player != null) {
      players.removeIf(onlinePlayer -> !onlinePlayer.getWorld().equals(player.getWorld()));
    }

    if (player != null) {
      players.removeIf(recipient -> {
        var jsonElement = Chatty.instance().get(JsonStorage.class)
          .getProperty(recipient, "ignore")
          .orElseGet(JsonArray::new);

        if (jsonElement.isJsonArray()) {
          for (var ignoreJsonElement : jsonElement.getAsJsonArray()) {
            if (player.getName().equalsIgnoreCase(ignoreJsonElement.getAsString())) {
              return true;
            }
          }
        }

        return false;
      });

      players.removeIf(recipient -> !Ranges.isApplicable(recipient, player, range));
    }

    players.removeIf(recipient ->
      !(recipient.equals(player)
        || !isPermissionRequired()
        // Don't remove!
        || recipient.hasPermission("chatty.chat." + name)
        || recipient.hasPermission("chatty.chat." + name + ".see"))
    );

    return players;
  }

  @Override
  public void sendMessage(String message, Predicate<Player> playerPredicate) {
    var stylishedMessage = TextUtil.stylish(message);
    getRecipients(null).stream()
      .filter(playerPredicate)
      .forEach(player -> player.sendMessage(stylishedMessage));

    Bukkit.getConsoleSender().sendMessage(TextUtil.stripHex(stylishedMessage));
  }

  @Override
  public void sendFormattedMessage(FormattedMessage formattedMessage, Predicate<Player> playerPredicate) {
    formattedMessage.send(getRecipients(null).stream().filter(playerPredicate).collect(Collectors.toSet()), null);
    formattedMessage.sendConsole();
  }

  public String getName() { return this.name; }
  public String getDisplayName() { return this.displayName; }
  public boolean isEnable() { return this.enable; }
  public String getFormat() { return this.format; }
  public int getRange() { return this.range; }
  public String getSymbol() { return this.symbol; }
  public long getCooldown() { return this.cooldown; }
  public int getMoney() { return this.money; }
  public @Nullable String getCommand() { return this.command; }
  public @Nullable List<String> getAliases() { return this.aliases; }
  public @Nullable Sound getSound() { return this.sound; }
  public boolean isSpyEnabled() { return this.spyEnabled; }
  public boolean isCapsModerationEnabled() { return this.capsModerationEnabled; }
  public boolean isSwearModerationEnabled() { return this.swearModerationEnabled; }
  public boolean isAdvertisementModerationEnabled() { return this.advertisementModerationEnabled; }
  public BukkitCommand getBukkitCommand() { return this.bukkitCommand; }
  public void setBukkitCommand(BukkitCommand bukkitCommand) { this.bukkitCommand = bukkitCommand; }

  public static class ChatBuilder {
    private String name;
    private String displayName;
    private boolean enable;
    private String format;
    private int range;
    private String symbol;
    private boolean permissionRequired;
    private long cooldown;
    private int money;
    private @Nullable String command;
    private @Nullable List<String> aliases;
    private @Nullable Sound sound;
    private boolean spyEnabled;
    private boolean capsModerationEnabled;
    private boolean swearModerationEnabled;
    private boolean advertisementModerationEnabled;
    private BukkitCommand bukkitCommand;

    ChatBuilder() { }

    public ChatBuilder name(String name) {
      this.name = name;
      return this;
    }

    public ChatBuilder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public ChatBuilder enable(boolean enable) {
      this.enable = enable;
      return this;
    }

    public ChatBuilder format(String format) {
      this.format = format;
      return this;
    }

    public ChatBuilder range(int range) {
      this.range = range;
      return this;
    }

    public ChatBuilder symbol(String symbol) {
      this.symbol = symbol;
      return this;
    }

    public ChatBuilder permissionRequired(boolean permissionRequired) {
      this.permissionRequired = permissionRequired;
      return this;
    }

    public ChatBuilder cooldown(long cooldown) {
      this.cooldown = cooldown;
      return this;
    }

    public ChatBuilder money(int money) {
      this.money = money;
      return this;
    }

    public ChatBuilder command(@Nullable String command) {
      this.command = command;
      return this;
    }

    public ChatBuilder aliases(@Nullable List<String> aliases) {
      this.aliases = aliases;
      return this;
    }

    public ChatBuilder sound(@Nullable Sound sound) {
      this.sound = sound;
      return this;
    }

    public ChatBuilder spyEnabled(boolean spyEnabled) {
      this.spyEnabled = spyEnabled;
      return this;

    }
    public ChatBuilder capsModerationEnabled(boolean capsModerationEnabled) {
      this.capsModerationEnabled = capsModerationEnabled;
      return this;
    }

    public ChatBuilder swearModerationEnabled(boolean swearModerationEnabled) {
      this.swearModerationEnabled = swearModerationEnabled;
      return this;
    }

    public ChatBuilder advertisementModerationEnabled(boolean advertisementModerationEnabled) {
      this.advertisementModerationEnabled = advertisementModerationEnabled;
      return this;
    }

    public ChatBuilder bukkitCommand(BukkitCommand bukkitCommand) {
      this.bukkitCommand = bukkitCommand;
      return this;
    }

    public Chat build() {
      return new Chat(
        name,
        displayName,
        enable, format,
        range, symbol,
        permissionRequired,
        cooldown,
        money,
        command,
        aliases,
        sound,
        spyEnabled,
        capsModerationEnabled,
        swearModerationEnabled,
        advertisementModerationEnabled,
        bukkitCommand
      );
    }

    public String toString() {
      return "Chat.ChatBuilder(name="
        + this.name
        + ", displayName="
        + this.displayName
        + ", enable="
        + this.enable
        + ", format="
        + this.format
        + ", range="
        + this.range
        + ", symbol="
        + this.symbol
        + ", permissionRequired="
        + this.permissionRequired
        + ", cooldown="
        + this.cooldown
        + ", money="
        + this.money
        + ", command="
        + this.command
        + ", aliases="
        + this.aliases
        + ", sound="
        + this.sound
        + ", spyEnabled="
        + this.spyEnabled
        + ", capsModerationEnabled="
        + this.capsModerationEnabled
        + ", swearModerationEnabled="
        + this.swearModerationEnabled
        + ", advertisementModerationEnabled="
        + this.advertisementModerationEnabled
        + ", bukkitCommand="
        + this.bukkitCommand
        + ')';
    }
  }
}
