package ru.mrbrikster.chatty.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.mrbrikster.chatty.api.chats.Chat;

public class ChattyMessageEvent extends Event {
  private static final HandlerList HANDLER_LIST = new HandlerList();

  private final Player player;
  private final Chat chat;
  private final String message;

  public ChattyMessageEvent(Player player, Chat chat,  String message) {
    super(true);
    this.player = player;
    this.chat = chat;
    this.message = message;
  }

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  /**
   * Returns the player that sends a message
   *
   * @return player that sends a message
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Returns the chat to which message sends
   *
   * @return chat to which message sends
   */
  public Chat getChat() {
    return chat;
  }

  /**
   * Returns the message typed by player
   *
   * @return message typed by player
   */
  public String getMessage() {
    return message;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }
}
