package ru.mrbrikster.chatty.dependencies;

import ru.mrbrikster.baseplugin.config.Configuration;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.chat.JsonStorage;

import lombok.Getter;

import java.util.logging.Level;

@Getter
public class DependencyManager {

    @Getter private VaultHook vault;
    @Getter private PlaceholderAPIHook placeholderApi;

    public DependencyManager(Chatty chatty) {
        Configuration configuration = chatty.getExact(Configuration.class);
        JsonStorage jsonStorage = chatty.getExact(JsonStorage.class);
        ChatManager chatManager = chatty.getExact(ChatManager.class);

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

}
