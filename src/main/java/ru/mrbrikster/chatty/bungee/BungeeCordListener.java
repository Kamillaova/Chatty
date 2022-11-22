package ru.mrbrikster.chatty.bungee;

import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import ru.mrbrikster.chatty.chat.ChatManager;
import ru.mrbrikster.chatty.json.fanciful.FancyMessage;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class BungeeCordListener implements PluginMessageListener {
  public final static UUID SERVER_UUID = UUID.randomUUID();

  private final ChatManager chatManager;

  public BungeeCordListener(ChatManager chatManager) {
    this.chatManager = chatManager;
  }

  @Override
  public void onPluginMessageReceived(String channel, Player player, byte[] message) {
    if (!channel.equals("BungeeCord")) {
      return;
    }

    var in = ByteStreams.newDataInput(message);
    var subchannel = in.readUTF();

    if (subchannel.equals("chatty")) {
      var length = in.readShort();
      var bytes = new byte[length];
      in.readFully(bytes);

      var inputStream = new DataInputStream(new ByteArrayInputStream(bytes));

      String chatName;
      String text;
      boolean json;

      try {
        chatName = inputStream.readUTF();

        var uuid = UUID.fromString(inputStream.readUTF());
        if (uuid.equals(SERVER_UUID)) return;

        text = inputStream.readUTF();
        json = inputStream.readBoolean();
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }

      var optionalChat = chatManager.getChats().stream().filter(c -> c.getName().equals(chatName)).findAny();

      if (optionalChat.isEmpty()) {
        return;
      }

      var chat = optionalChat.get();

      if (chat.getRange() > -3) {
        return;
      }

      if (json) {
        var fancyMessage = FancyMessage.deserialize(text);
        fancyMessage.send(Bukkit.getOnlinePlayers().stream()
          .filter(recipient -> !chat.isPermissionRequired() ||
            recipient.hasPermission("chatty.chat." + chat.getName() + ".see") ||
            recipient.hasPermission("chatty.chat." + chat.getName())
          ).toList(), null);

        fancyMessage.send(Bukkit.getConsoleSender(), null);
      } else {
        Bukkit.getOnlinePlayers().stream()
          .filter(recipient -> !chat.isPermissionRequired()
            || recipient.hasPermission("chatty.chat." + chat.getName() + ".see")
            || recipient.hasPermission("chatty.chat." + chat.getName()))
          .forEach(onlinePlayer -> onlinePlayer.sendMessage(text));

        Bukkit.getConsoleSender().sendMessage(text);
      }
    }
  }
}
