package ru.mrbrikster.chatty.api;

import ru.mrbrikster.chatty.api.chats.Chat;

import java.util.Collection;
import java.util.Optional;

public interface ChattyApi {
  static ChattyApi get() {
    return ChattyApiHolder.api();
  }

  /**
   * Returns collection of enabled chats
   *
   * @return collection of enabled chats
   */
  Collection<Chat> chats();

  /**
   * Returns chat with given name, if exists
   *
   * @param name chat name
   * @return optional chat with given name
   */
  Optional<Chat> getChat(String name);

  class ChattyApiHolder {
    private static ChattyApi api;

    public static ChattyApi api() {
      return api;
    }
    public static void api(ChattyApi api) {
      ChattyApiHolder.api = api;
    }
  }
}
