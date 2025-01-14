package ru.mrbrikster.chatty.moderation;

import ru.mrbrikster.baseplugin.config.ConfigurationNode;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

public class CapsModerationMethod extends ModerationMethod {
  private final int percent;
  private final int length;
  private final boolean useBlock;

  CapsModerationMethod(ConfigurationNode configurationNode, String message) {
    super(message);

    this.useBlock = configurationNode.getNode("block").getAsBoolean(true);
    this.percent = configurationNode.getNode("percent").getAsInt(80);
    this.length = configurationNode.getNode("length").getAsInt(6);
  }

  @Override
  public String getEditedMessage() {
    return message.toLowerCase();
  }

  @Override
  public boolean isBlocked() {
    return message.length() >= length && getPercent() >= percent;
  }

  @Override
  public String getLogPrefix() {
    return "CAPS";
  }

  @Override
  public String getWarningMessageKey() {
    return "caps-found";
  }

  private double getPercent() {
    int codePoint, length = 0, capsLength = 0;
    for (var c : message.toCharArray()) {
      codePoint = c;
      if (Character.isLetter(codePoint)) {
        length++;
        if (codePoint == toUpperCase(codePoint) && (toLowerCase(codePoint) != toUpperCase(codePoint))) {
          capsLength++;
        }
      }
    }

    return (double) capsLength / (double) length * 100;
  }

  public boolean isUseBlock() { return this.useBlock; }
}
