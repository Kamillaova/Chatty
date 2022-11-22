package ru.mrbrikster.chatty.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.Chatty;

public class BungeeBroadcaster {
  public static void broadcast(Player player, String chat, String message, boolean json) {
    var out = ByteStreams.newDataOutput();

    /*
      Forward message allows to share custom plugin data between BungeeCord servers
      https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#forward
    */
    out.writeUTF("Forward");
    out.writeUTF("ALL");
    out.writeUTF("chatty");

    var messageStream = ByteStreams.newDataOutput();
    messageStream.writeUTF(chat);
    messageStream.writeUTF(BungeeCordListener.SERVER_UUID.toString());
    messageStream.writeUTF(message);
    messageStream.writeBoolean(json);

    var bytes = messageStream.toByteArray();

    out.writeShort(bytes.length);
    out.write(bytes);

    player.sendPluginMessage(Chatty.instance(), "BungeeCord", out.toByteArray());
  }
}
