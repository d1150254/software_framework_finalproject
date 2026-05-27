package org.example.core;

/**
 * Represents a method (function) entity in a UML Class or Interface.
 */
public class UMLMethod extends UMLMember {
    private static final String DEFAULT_VISIBILITY = "+";
    private static final String PARSE_REGEX = "^([+\\-#~])?\\s*(\\w+)\\s*\\((.*)\\)\\s*(?::\\s*(\\w+))?$";
    private static final String ERROR_MSG_FORMAT = "方法格式錯誤。參考格式：+ name(parameters): returnType";

    private String parameters = "";
    private String returnType = "";

    public UMLMethod(String name) {
        super(name);
        this.visibility = DEFAULT_VISIBILITY;
    }

    public UMLMethod(String visibility, String name, String parameters, String returnType) {
        super(visibility, name);
        this.parameters = parameters;
        this.returnType = returnType;
    }

    @Override
    public String getDisplayText() {
        StringBuilder sb = new StringBuilder();
        if (visibility != null && !visibility.isEmpty()) sb.append(visibility).append(" ");
        sb.append(name).append("(").append(parameters).append(")");
        if (returnType != null && !returnType.isEmpty()) sb.append(": ").append(returnType);
        return sb.toString();
    }
    
    public static UMLMethod parse(String input) {
        if (isInvalidInput(input)) {
            throw new IllegalArgumentException(ERROR_MSG_FORMAT);
        }
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(PARSE_REGEX);
        java.util.regex.Matcher matcher = pattern.matcher(input.trim());
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException(ERROR_MSG_FORMAT);
        }
        
        return extractMethodFromMatcher(matcher);
    }

    private static UMLMethod extractMethodFromMatcher(java.util.regex.Matcher matcher) {
        String vis = matcher.group(1);
        if (vis == null) vis = DEFAULT_VISIBILITY;
        String extractedName = matcher.group(2);
        String params = matcher.group(3);
        String ret = matcher.group(4);
        
        return new UMLMethod(vis, extractedName, params == null ? "" : params, ret == null ? "" : ret);
    }
}
