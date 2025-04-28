package myutil;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Localization {
    private static ResourceBundle bundle;

    // Initialize ResourceBundle
    public static void init(Locale locale) {
        bundle = ResourceBundle.getBundle("assets.bg369.lang.bg369", locale);
    }

    // Get localized text based on key value
    public static String getString(String key, Object... args) {
        String value = bundle.getString(key);
        value = resolveNestedKeys(value); // Resolve nested keys
        try {
            if (args.length > 0) {
                value = MessageFormat.format(value, args); // Handle placeholders
            }
        } catch (Exception e) {
            System.out.println("解析鍵值嵌套時發生錯誤: " + e.getMessage());
            System.out.println("鍵值: " + value + " args: " + Arrays.toString(args));
            
        }
        return value;
    }

    // Resolve nested keys
    private static String resolveNestedKeys(String value) {
        Pattern pattern = Pattern.compile("\\{(?!\\d+\\})[^}]+\\}");
        Matcher matcher = pattern.matcher(value);
        StringBuffer result = new StringBuffer();
        try {
            while (matcher.find()) {
                String nestedKey = matcher.group(0).substring(1, matcher.group(0).length() - 1);
                String nestedValue = bundle.getString(nestedKey);
                matcher.appendReplacement(result, nestedValue);
            }
            matcher.appendTail(result);
        } catch (Exception e) {
            System.out.println("解析鍵值嵌套時發生錯誤: " + e.getMessage());
            System.out.println("鍵值: " + value + " matcher: " + matcher.toString() + " find: " + matcher.find() + " group: " + matcher.group(0) + " result: " + result.toString());
        }
        return result.toString();
    }
}
