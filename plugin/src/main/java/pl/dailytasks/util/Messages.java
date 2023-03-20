package pl.dailytasks.util;

import lombok.Getter;

import java.util.HashMap;

@Getter
public class Messages {

    private final HashMap<String, String> messages = new HashMap<>(); // List of all messages from messages.yml
    private final HashMap<String, String> translations = new HashMap<>(); // List of translations

    public String getMessage(String path) {
        if(messages.containsKey(path)) {
            return messages.get(path);
        } else {
            return "§cCannotRead exception for path \"" + path + "\"";
        }
    }

    public String getTranslation(String path) {
        return translations.getOrDefault(path, path);
    }

    public void addMessage(String key, String message) {
        messages.put(key, message);
    }

    public void addTranslation(String key, String message) {
        translations.put(key, message);
    }

    public void clearMessages() {
        messages.clear();
    }

    public void clearTranslations() {
        translations.clear();
    }
}
