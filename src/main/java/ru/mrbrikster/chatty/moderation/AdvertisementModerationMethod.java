package ru.mrbrikster.chatty.moderation;

import lombok.Getter;
import ru.mrbrikster.baseplugin.config.ConfigurationNode;
import ru.mrbrikster.chatty.util.CachedObject;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class AdvertisementModerationMethod extends ModifyingSubstringsModerationMethod {
  private static final String IP =
    "\\b((\\d{1,2}|2(5[0-5]|[0-4]\\d))[._,)(-]+){3}(\\d{1,2}|2(5[0-5]|[0-4]\\d))(:\\d{2,7})?";
  private static final String WEB =
    "(?i)\\b(https?:\\/\\/)?[\\w\\.а-яА-Я-]+\\.([a-z]{2,4}|[рР][фФ]|[уУ][кК][рР])\\b(:\\d{2,7})?(\\/\\S+)?";
  private static final CachedObject<String, Pattern> cachedIp = new CachedObject<>(IP, Pattern.compile(IP));
  private static final CachedObject<String, Pattern> cachedWeb = new CachedObject<>(WEB, Pattern.compile(WEB));
  private final Set<String> whitelist;
  private final Pattern ipPattern;
  private final Pattern webPattern;
  @Getter
  private final String replacement;
  @Getter
  private final boolean useBlock;
  private String editedMessage;
  private boolean checked = false, result = false;

  AdvertisementModerationMethod(ConfigurationNode configurationNode, String message, String lastFormatColors) {
    super(message, lastFormatColors);

    this.whitelist = new HashSet<>(configurationNode.getNode("whitelist").getAsStringList());
    this.useBlock = configurationNode.getNode("block").getAsBoolean(true);
    this.replacement = TextUtil.stylish(configurationNode.getNode("replacement").getAsString("<ads>"));

    var ipString = configurationNode.getNode("patterns.ip").getAsString(IP);
    var webString = configurationNode.getNode("patterns.web").getAsString(WEB);
    this.ipPattern = cachedIp.get(ipString, () -> Pattern.compile(ipString));
    this.webPattern = cachedWeb.get(webString, () -> Pattern.compile(webString));
  }

  @Override
  public String getEditedMessage() {
    if (this.editedMessage == null) {
      this.isBlocked();
    }

    return this.editedMessage;
  }

  @Override
  public boolean isBlocked() {
    if (this.checked) {
      return this.result;
    }

    if (this.editedMessage == null) {
      this.editedMessage = this.message;
    }

    this.result = match(ipPattern) | match(webPattern) || this.result;

    this.checked = true;

    return this.result;
  }

  @Override
  public String getLogPrefix() {
    return "ADVERTISEMENT";
  }

  @Override
  public String getWarningMessageKey() {
    return "advertisement-found";
  }

  private boolean match(Pattern pattern) {
    var matcher = pattern.matcher(this.editedMessage);

    var prevIndex = 0;
    var builder = new StringBuilder();

    var containsAds = false;
    while (matcher.find()) {
      var group = matcher.group();

      builder.append(this.editedMessage, prevIndex, matcher.start());
      prevIndex = matcher.end();

      if (this.whitelist.contains(group.trim())) {
        builder.append(this.editedMessage, matcher.start(), matcher.end());
      } else {
        containsAds = true;

        var lastColors = TextUtil.getLastColors(message.substring(0, matcher.start()));
        if (lastColors.isEmpty()) lastColors = lastFormatColors;

        builder.append(this.replacement).append(lastColors);
      }
    }

    if (prevIndex < this.editedMessage.length()) {
      builder.append(this.editedMessage, prevIndex, this.editedMessage.length());
    }

    this.editedMessage = builder.toString();
    return containsAds;
  }
}
