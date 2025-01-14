package ru.mrbrikster.chatty.notifications;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.dependencies.DependencyManager;
import ru.mrbrikster.chatty.util.Pair;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class ChatNotification extends Notification {

  private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "chat.%s";
  @SuppressWarnings("deprecation")
  private static final JsonParser JsonParser = new JsonParser();

  private final String name;
  private final List<List<Pair<String, Boolean>>> messages = new ArrayList<>();

  @SuppressWarnings("deprecation")
  ChatNotification(String name, int delay, String prefix, List<String> messages, boolean permission, boolean random) {
    super(delay, permission, messages.size(), random);

    this.name = name;
    this.messages.clear();

    for (var message : messages) {
      message = TextUtil.fixMultilineFormatting(message);

      var lines = message.split("(\n)|(\\\\n)");

      List<Pair<String, Boolean>> formattedLines = new ArrayList<>();
      for (var line : lines) {
        try {
          var jsonObject = JsonParser.parse(line).getAsJsonObject();
          formattedLines.add(Pair.of(jsonObject.toString(), true));
        } catch (JsonSyntaxException | IllegalStateException exception) {
          formattedLines.add(Pair.of(TextUtil.stylish(prefix + line), false));
        }
      }

      this.messages.add(formattedLines);
    }
  }

  @Override
  public void run() {
    if (messages.isEmpty()) {
      return;
    }

    var lines = messages.get(nextMessage());

    var dependencyManager = Chatty.instance().get(DependencyManager.class);
    Bukkit.getOnlinePlayers().stream()
      .filter(player -> !isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name)))
      .forEach(player -> lines.forEach(line -> {
        var formattedLine = dependencyManager.getPlaceholderApi() != null
                              ? dependencyManager.getPlaceholderApi().setPlaceholders(player, line.left())
                              : line.left();
          player.sendMessage(formattedLine);
      }));
  }
}
