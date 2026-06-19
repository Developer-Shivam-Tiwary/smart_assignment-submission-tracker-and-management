package com.smartassignment.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Lightweight, zero-dependency JSON serialization and parsing utility.
 * Designed to replace Jackson/Gson in standalone Core Java environments.
 */
public class JsonUtil {

    // =========================================================================
    // SERIALIZATION (Object -> JSON String)
    // =========================================================================

    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "\"" + escapeString((String) obj) + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Character) {
            return "\"" + escapeString(obj.toString()) + "\"";
        }
        if (obj instanceof Date) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "\"" + sdf.format((Date) obj) + "\"";
        }
        if (obj instanceof java.time.temporal.Temporal) {
            return "\"" + obj.toString() + "\"";
        }
        if (obj.getClass().isEnum()) {
            return "\"" + obj.toString() + "\"";
        }
        if (obj instanceof Collection) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : (Collection<?>) obj) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeString(entry.getKey().toString())).append("\":");
                sb.append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj.getClass().isArray()) {
            StringBuilder sb = new StringBuilder("[");
            int len = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(java.lang.reflect.Array.get(obj, i)));
            }
            sb.append("]");
            return sb.toString();
        }

        // Custom OOPS class serialization using reflection
        return serializeCustomObject(obj);
    }

    private static String serializeCustomObject(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Class<?> clazz = obj.getClass();
        List<Field> allFields = new ArrayList<>();
        
        // Traverse class hierarchy to get all fields
        while (clazz != null && clazz != Object.class) {
            allFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        boolean first = true;
        for (Field field : allFields) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (!first) sb.append(",");
                sb.append("\"").append(field.getName()).append("\":");
                sb.append(toJson(value));
                first = false;
            } catch (IllegalAccessException e) {
                // Ignore inaccessible fields
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escapeString(String str) {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < ' ') {
                        String hex = Integer.toHexString(ch);
                        sb.append("\\u").append("0".repeat(4 - hex.length())).append(hex);
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // PARSING (JSON String -> Map/List/Object)
    // =========================================================================

    public static Map<String, Object> parseObject(String json) {
        Object parsed = new JsonParser(json).parse();
        if (parsed instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) parsed;
            return map;
        }
        throw new IllegalArgumentException("Input string is not a JSON object");
    }

    public static List<Object> parseList(String json) {
        Object parsed = new JsonParser(json).parse();
        if (parsed instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) parsed;
            return list;
        }
        throw new IllegalArgumentException("Input string is not a JSON array");
    }

    public static Object parse(String json) {
        return new JsonParser(json).parse();
    }

    private static class JsonParser {
        private final String src;
        private int pos = 0;

        public JsonParser(String src) {
            this.src = src != null ? src.trim() : "";
        }

        public Object parse() {
            skipWhitespace();
            if (pos >= src.length()) {
                return null;
            }
            char c = src.charAt(pos);
            if (c == '{') {
                return parseMap();
            } else if (c == '[') {
                return parseArray();
            } else if (c == '"') {
                return parseString();
            } else if (Character.isDigit(c) || c == '-') {
                return parseNumber();
            } else if (src.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            } else if (src.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            } else if (src.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + pos);
        }

        private Map<String, Object> parseMap() {
            pos++; // Skip '{'
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == '}') {
                pos++; // Skip '}'
                return map;
            }

            while (pos < src.length()) {
                skipWhitespace();
                if (src.charAt(pos) != '"') {
                    throw new IllegalArgumentException("Expected string key in object at position " + pos);
                }
                String key = parseString();
                skipWhitespace();
                if (pos >= src.length() || src.charAt(pos) != ':') {
                    throw new IllegalArgumentException("Expected ':' after key in object at position " + pos);
                }
                pos++; // Skip ':'
                Object val = parse();
                map.put(key, val);
                skipWhitespace();
                if (pos >= src.length()) {
                    throw new IllegalArgumentException("Unterminated object");
                }
                char next = src.charAt(pos);
                if (next == '}') {
                    pos++; // Skip '}'
                    break;
                } else if (next == ',') {
                    pos++; // Skip ','
                } else {
                    throw new IllegalArgumentException("Expected ',' or '}' in object at position " + pos);
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            pos++; // Skip '['
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ']') {
                pos++; // Skip ']'
                return list;
            }

            while (pos < src.length()) {
                list.add(parse());
                skipWhitespace();
                if (pos >= src.length()) {
                    throw new IllegalArgumentException("Unterminated array");
                }
                char next = src.charAt(pos);
                if (next == ']') {
                    pos++; // Skip ']'
                    break;
                } else if (next == ',') {
                    pos++; // Skip ','
                } else {
                    throw new IllegalArgumentException("Expected ',' or ']' in array at position " + pos);
                }
            }
            return list;
        }

        private String parseString() {
            pos++; // Skip opening '"'
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if (c == '"') {
                    pos++; // Skip closing '"'
                    return sb.toString();
                } else if (c == '\\') {
                    pos++;
                    if (pos >= src.length()) throw new IllegalArgumentException("Unescaped trailing backslash");
                    char esc = src.charAt(pos);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (pos + 4 >= src.length()) throw new IllegalArgumentException("Invalid unicode escape");
                            String hex = src.substring(pos + 1, pos + 5);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                            break;
                        default: sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
                pos++;
            }
            throw new IllegalArgumentException("Unterminated string starting at " + pos);
        }

        private Number parseNumber() {
            int start = pos;
            if (src.charAt(pos) == '-') {
                pos++;
            }
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.' || src.charAt(pos) == 'e' || src.charAt(pos) == 'E' || src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                pos++;
            }
            String numStr = src.substring(start, pos);
            if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                return Double.parseDouble(numStr);
            } else {
                try {
                    return Integer.parseInt(numStr);
                } catch (NumberFormatException e) {
                    return Long.parseLong(numStr);
                }
            }
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }
    }
}
