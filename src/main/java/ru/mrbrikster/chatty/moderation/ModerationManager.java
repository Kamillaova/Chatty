package ru.mrbrikster.chatty.moderation;

import org.bukkit.plugin.java.JavaPlugin;
import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;

public class ModerationManager {
  private final JavaPlugin javaPlugin;
  private final Configuration configuration;
  private boolean capsModerationEnabled;
  private boolean advertisementModerationEnabled;
  private boolean swearModerationEnabled;

  public ModerationManager(Chatty chatty) {
    this.javaPlugin = chatty;
    this.configuration = chatty.get(Configuration.class);

    init();
    configuration.onReload(config -> reload());
  }

  private void init() {
    var moderationNode =
      configuration.getNode("moderation");

    this.capsModerationEnabled = moderationNode.getNode("caps.enable")
      .getAsBoolean(false);

    this.advertisementModerationEnabled = moderationNode.getNode("advertisement.enable")
      .getAsBoolean(false);

    this.swearModerationEnabled = moderationNode.getNode("swear.enable")
      .getAsBoolean(false);

    if (swearModerationEnabled) {
      SwearModerationMethod.init(javaPlugin);
    }
  }

  private void reload() {
    init();
  }

  public CapsModerationMethod getCapsMethod(String message) {
    return new CapsModerationMethod(configuration.getNode("moderation.caps"), message);
  }

  public AdvertisementModerationMethod getAdvertisementMethod(String message, String lastFormatColors) {
    return new AdvertisementModerationMethod(configuration.getNode("moderation.advertisement"), message, lastFormatColors);
  }

  public SwearModerationMethod getSwearMethod(String message, String lastFormatColors) {
    return new SwearModerationMethod(configuration.getNode("moderation.swear"), message, lastFormatColors);
  }

  public boolean isCapsModerationEnabled() { return this.capsModerationEnabled; }
  public boolean isAdvertisementModerationEnabled() { return this.advertisementModerationEnabled; }
  public boolean isSwearModerationEnabled() { return this.swearModerationEnabled; }
}
