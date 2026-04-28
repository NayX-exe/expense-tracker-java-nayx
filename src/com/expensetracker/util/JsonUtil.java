package com.expensetracker.util;

import java.util.*;

/**
 * Minimal hand-written JSON parser/writer — no external dependencies.
 */
public class JsonUtil {

    // ─── Writer ───────────────────────────────────────────────────────────────

    public static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escapeStr((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            return sb.append("]").toString();
        }
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeStr(e.getKey())).append("\":").append(toJson(e.getValue()));
            }
            return sb.append("}").toString();
        }
        return "\"" + escapeStr(obj.toString()) + "\"";
    }

    private static String escapeStr(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // ─── Parser ───────────────────────────────────────────────────────────────

    private String src;
    private int pos;

    private JsonUtil(String src) {
        this.src = src;
        this.pos = 0;
    }

    public static Object parse(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        return new JsonUtil(json.trim()).parseValue();
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> parseArray(String json) {
        Object o = parse(json);
        if (o instanceof List) return (List<Map<String, Object>>) o;
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object o = parse(json);
        if (o instanceof Map) return (Map<String, Object>) o;
        return new LinkedHashMap<>();
    }

    private Object parseValue() {
        skipWS();
        if (pos >= src.length()) return null;
        char c = src.charAt(pos);
        if (c == '{') return parseObject2();
        if (c == '[') return parseArray2();
        if (c == '"') return parseString();
        if (c == 't') { pos += 4; return Boolean.TRUE; }
        if (c == 'f') { pos += 5; return Boolean.FALSE; }
        if (c == 'n') { pos += 4; return null; }
        return parseNumber();
    }

    private Map<String, Object> parseObject2() {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++; // '{'
        skipWS();
        if (pos < src.length() && src.charAt(pos) == '}') { pos++; return map; }
        while (pos < src.length()) {
            skipWS();
            String key = parseString();
            skipWS();
            pos++; // ':'
            Object val = parseValue();
            map.put(key, val);
            skipWS();
            if (pos < src.length() && src.charAt(pos) == '}') { pos++; break; }
            if (pos < src.length() && src.charAt(pos) == ',') pos++;
        }
        return map;
    }

    private List<Object> parseArray2() {
        List<Object> list = new ArrayList<>();
        pos++; // '['
        skipWS();
        if (pos < src.length() && src.charAt(pos) == ']') { pos++; return list; }
        while (pos < src.length()) {
            list.add(parseValue());
            skipWS();
            if (pos < src.length() && src.charAt(pos) == ']') { pos++; break; }
            if (pos < src.length() && src.charAt(pos) == ',') pos++;
        }
        return list;
    }

    private String parseString() {
        pos++; // opening "
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') break;
            if (c == '\\') {
                char esc = src.charAt(pos++);
                switch (esc) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Number parseNumber() {
        int start = pos;
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == ',' || c == '}' || c == ']' || c == ' ' || c == '\n' || c == '\r' || c == '\t') break;
            pos++;
        }
        String num = src.substring(start, pos);
        if (num.contains(".")) return Double.parseDouble(num);
        return Long.parseLong(num);
    }

    private void skipWS() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }

    // ─── Pretty print ─────────────────────────────────────────────────────────

    public static String pretty(String json) {
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) inString = !inString;
            if (inString) { sb.append(c); continue; }
            switch (c) {
                case '{': case '[':
                    sb.append(c).append("\n").append("  ".repeat(++indent));
                    break;
                case '}': case ']':
                    sb.append("\n").append("  ".repeat(--indent)).append(c);
                    break;
                case ',':
                    sb.append(c).append("\n").append("  ".repeat(indent));
                    break;
                case ':':
                    sb.append(": ");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}