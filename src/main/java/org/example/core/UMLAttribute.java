package org.example.core;

/**
 * Represents an attribute (field) entity in a UML Class.
 */
public class UMLAttribute {
    private static final String DEFAULT_VISIBILITY = "+";
    private static final String PARSE_REGEX = "^([+\\-#~])?\\s*(\\w+)\\s*(?::\\s*(\\w+))?\\s*(?:=\\s*(.+))?$";
    private static final String ERROR_MSG_FORMAT = "屬性格式錯誤。參考格式：+ name: type = defaultValue";

    private String visibility = DEFAULT_VISIBILITY;
    private String name;
    private String type = "";
    private String defaultValue = "";

    public UMLAttribute(String name) {
        this.name = name;
    }

    public UMLAttribute(String visibility, String name, String type, String defaultValue) {
        this.visibility = visibility;
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayText() {
        StringBuilder sb = new StringBuilder();
        if (visibility != null && !visibility.isEmpty()) sb.append(visibility).append(" ");
        sb.append(name);
        if (type != null && !type.isEmpty()) sb.append(": ").append(type);
        if (defaultValue != null && !defaultValue.isEmpty()) sb.append(" = ").append(defaultValue);
        return sb.toString();
    }
    
    public static UMLAttribute parse(String input) {
        if (isInvalidInput(input)) {
            throw new IllegalArgumentException(ERROR_MSG_FORMAT);
        }
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(PARSE_REGEX);
        java.util.regex.Matcher matcher = pattern.matcher(input.trim());
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException(ERROR_MSG_FORMAT);
        }
        
        return extractAttributeFromMatcher(matcher);
    }

    private static boolean isInvalidInput(String input) {
        return input == null || input.trim().isEmpty();
    }

    private static UMLAttribute extractAttributeFromMatcher(java.util.regex.Matcher matcher) {
        String vis = matcher.group(1);
        if (vis == null) vis = DEFAULT_VISIBILITY;
        String extractedName = matcher.group(2);
        String extractedType = matcher.group(3);
        String extractedDefault = matcher.group(4);
        
        return new UMLAttribute(vis, extractedName, extractedType == null ? "" : extractedType, extractedDefault == null ? "" : extractedDefault);
    }
}
