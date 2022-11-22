package ru.mrbrikster.chatty.json;

import ru.mrbrikster.chatty.json.fanciful.FancyMessage;
import ru.mrbrikster.chatty.util.TextUtil;

public class LegacyMessagePart implements MessagePart {
  private final String text;
  private final boolean colorize;

  public LegacyMessagePart(String text) {
    this.text = text;
    this.colorize = true;
  }

  public LegacyMessagePart(String text, boolean colorize) {
    this.text = text;
    this.colorize = colorize;
  }

  @Override
  public FancyMessage append(FancyMessage fancyMessage) {
    LegacyConverter.getMessageParts(fancyMessage.getLastColors() + (colorize ? TextUtil.stylish(text) : text))
      .forEach(fancyMessage::then);

    return fancyMessage;
  }

  public String getText() {
    return text;
  }
}
