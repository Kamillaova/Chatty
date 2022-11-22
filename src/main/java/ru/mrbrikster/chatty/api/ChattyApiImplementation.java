package ru.mrbrikster.chatty.api;

import ru.mrbrikster.chatty.api.chats.Chat;

import java.util.Collection;
import java.util.Optional;

public record ChattyApiImplementation(Collection<Chat> chats) implements ChattyApi {
  @Override
  public Optional<Chat> getChat(String name) {
    return chats().stream()
      .filter(chat -> chat.getName().equalsIgnoreCase(name))
      .findAny();
  }
}
