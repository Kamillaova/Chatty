package ru.mrbrikster.chatty.dependencies;

import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.ChatManager;

import java.util.logging.Level;

public class DependencyManager {
  private VaultHook vault;
  private PlaceholderAPIHook placeholderApi;

  public DependencyManager(Chatty chatty) {
    var chatManager = chatty.get(ChatManager.class);

    if (chatty.getServer().getPluginManager().isPluginEnabled("Vault")) {
      this.vault = new VaultHook();
      chatty.getLogger().log(Level.INFO, "Vault has successful hooked.");
    }

    if (chatty.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      this.placeholderApi = new PlaceholderAPIHook(chatManager);
      placeholderApi.register();
      chatty.getLogger().log(Level.INFO, "PlaceholderAPI has successful hooked.");
    }
  }

  public VaultHook getVault() { return this.vault; }
  public PlaceholderAPIHook getPlaceholderApi() { return this.placeholderApi; }
}
