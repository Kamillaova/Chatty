package ru.mrbrikster.chatty.chat.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.mrbrikster.chatty.chat.Chat;

import java.util.HashSet;

public class ChattyAsyncPlayerChatEvent extends AsyncPlayerChatEvent {
  private final Chat chat;

  /**
   * @param who     the chat sender
   * @param message the message sent
   * @param chat    the chat to send message
   */
  public ChattyAsyncPlayerChatEvent( Player who,  String message, Chat chat) {
    super(true, who, message, new HashSet<>(Bukkit.getOnlinePlayers()));

    this.chat = chat;
  }

  public Chat chat() {
    return chat;
  }
}
