package ru.mrbrikster.chatty.json;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ru.mrbrikster.chatty.json.fanciful.FancyMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormattedMessage {
  private List<MessagePart> messageParts = new ArrayList<>();

  public FormattedMessage() { }

  public FormattedMessage(String text) {
    this.messageParts.add(new LegacyMessagePart(text));
  }

  public FormattedMessage(String text, boolean colorize) {
    this.messageParts.add(new LegacyMessagePart(text, colorize));
  }

  public FormattedMessage send(Collection<? extends Player> players, Player sender) {
    toFancyMessage().send(players, sender);

    return this;
  }

  public void sendConsole() {
    Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(toFancyMessage().toOldMessageFormat()));
  }

  public FormattedMessage append(FormattedMessage formattedMessage) {
    this.messageParts.addAll(formattedMessage.messageParts);

    return this;
  }

  public FormattedMessage append(MessagePart messagePart) {
    this.messageParts.add(messagePart);

    return this;
  }

  public FormattedMessage replace(Pattern pattern, FormattedMessage message) {
    return replace(pattern, message.messageParts);
  }

  public FormattedMessage replace(String text, FormattedMessage message) {
    return replace(Pattern.compile(Pattern.quote(text)), message.messageParts);
  }

  public FormattedMessage replace(String text, MessagePart... parts) {
    return replace(Pattern.compile(Pattern.quote(text)), Arrays.asList(parts));
  }

  public FormattedMessage replace(Pattern pattern, MessagePart... parts) {
    return replace(pattern, Arrays.asList(parts));
  }

  public FormattedMessage replace(String text, List<MessagePart> parts) {
    return replace(Pattern.compile(Pattern.quote(text)), parts);
  }

  /**
   * EXPERIMENTAL
   * Rewritten function, that supports multiple parts and should be more stable and effective
   *
   * @return this instance of FormattedMessage
   */
  public FormattedMessage replace(Pattern pattern, List<MessagePart> parts) {
    List<MessagePart> updatedMessageParts = new ArrayList<>();

    for (var messagePart : messageParts) {
      if (messagePart instanceof LegacyMessagePart legacyPart) {
        var partText = legacyPart.getText();
        var matcher = pattern.matcher(partText);

        var firstIndex = 0;
        while (matcher.find()) {
          updatedMessageParts.add(new LegacyMessagePart(partText.substring(firstIndex, matcher.start())));
          updatedMessageParts.addAll(parts);
          firstIndex = matcher.end();
        }

        var tail = partText.substring(firstIndex);

        if (!tail.isEmpty()) {
          updatedMessageParts.add(new LegacyMessagePart(tail));
        }
      } else {
        updatedMessageParts.add(messagePart);
      }
    }

    this.messageParts = updatedMessageParts;

    return this;
  }

  public String getLastColors() {
    return toFancyMessage().getLastColors();
  }

  public FancyMessage toFancyMessage() {
    var fancyMessage = new FancyMessage("");

    for (var messagePart : messageParts) {
      fancyMessage = messagePart.append(fancyMessage);
    }

    fancyMessage.getMessageParts().removeIf(messagePart ->
      messagePart.text.toString().isEmpty()
    );

    return fancyMessage;
  }

  public String toReadableText() {
    return toFancyMessage().toOldMessageFormat();
  }

  public String toJSONString() {
    return toFancyMessage().toJSONString();
  }
}
