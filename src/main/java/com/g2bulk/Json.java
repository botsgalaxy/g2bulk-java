package com.g2bulk;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Json {
    private final String s;
    private int i;

    private Json(String s) {
        this.s = s;
    }

    static Object parse(String text) {
        Json p = new Json(text);
        p.ws();
        Object v = p.value();
        p.ws();
        if (p.i != p.s.length()) throw p.error("Unexpected trailing data");
        return v;
    }

    static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    private IllegalArgumentException error(String msg) {
        return new IllegalArgumentException(msg + " at position " + i);
    }

    private void ws() {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') i++;
            else break;
        }
    }

    private Object value() {
        if (i >= s.length()) throw error("Unexpected end of input");
        char c = s.charAt(i);
        switch (c) {
            case '{':
                return object();
            case '[':
                return array();
            case '"':
                return string();
            case 't':
                literal("true");
                return Boolean.TRUE;
            case 'f':
                literal("false");
                return Boolean.FALSE;
            case 'n':
                literal("null");
                return null;
            default:
                if (c == '-' || (c >= '0' && c <= '9')) return number();
                throw error("Unexpected character '" + c + "'");
        }
    }

    private void literal(String word) {
        if (!s.startsWith(word, i)) throw error("Invalid literal");
        i += word.length();
    }

    private Map<String, Object> object() {
        Map<String, Object> map = new LinkedHashMap<>();
        i++;
        ws();
        if (i < s.length() && s.charAt(i) == '}') {
            i++;
            return map;
        }
        while (true) {
            ws();
            if (i >= s.length() || s.charAt(i) != '"') throw error("Expected string key");
            String key = string();
            ws();
            if (i >= s.length() || s.charAt(i) != ':') throw error("Expected ':'");
            i++;
            ws();
            map.put(key, value());
            ws();
            if (i >= s.length()) throw error("Unterminated object");
            char c = s.charAt(i++);
            if (c == '}') return map;
            if (c != ',') throw error("Expected ',' or '}'");
        }
    }

    private List<Object> array() {
        List<Object> list = new ArrayList<>();
        i++;
        ws();
        if (i < s.length() && s.charAt(i) == ']') {
            i++;
            return list;
        }
        while (true) {
            ws();
            list.add(value());
            ws();
            if (i >= s.length()) throw error("Unterminated array");
            char c = s.charAt(i++);
            if (c == ']') return list;
            if (c != ',') throw error("Expected ',' or ']'");
        }
    }

    private String string() {
        i++;
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (i >= s.length()) throw error("Unterminated string");
            char c = s.charAt(i++);
            if (c == '"') return sb.toString();
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (i >= s.length()) throw error("Unterminated escape");
            char e = s.charAt(i++);
            switch (e) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    if (i + 4 > s.length()) throw error("Invalid unicode escape");
                    try {
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    } catch (NumberFormatException ex) {
                        throw error("Invalid unicode escape");
                    }
                    i += 4;
                    break;
                default:
                    throw error("Invalid escape");
            }
        }
    }

    private Object number() {
        int start = i;
        if (s.charAt(i) == '-') i++;
        boolean decimal = false;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') i++;
            else if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                decimal = true;
                i++;
            } else break;
        }
        String num = s.substring(start, i);
        try {
            if (!decimal) {
                BigInteger big = new BigInteger(num);
                if (big.bitLength() < 64) return big.longValue();
                return big;
            }
            return Double.parseDouble(num);
        } catch (NumberFormatException ex) {
            throw error("Invalid number '" + num + "'");
        }
    }

    private static void write(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
        } else if (v instanceof String) {
            quote(sb, (String) v);
        } else if (v instanceof Boolean) {
            sb.append(v.toString());
        } else if (v instanceof Double || v instanceof Float) {
            double d = ((Number) v).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) sb.append("null");
            else if (d == Math.rint(d) && Math.abs(d) < 1e15) sb.append((long) d);
            else sb.append(BigDecimal.valueOf(d).stripTrailingZeros().toPlainString());
        } else if (v instanceof Number) {
            sb.append(v.toString());
        } else if (v instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                quote(sb, String.valueOf(e.getKey()));
                sb.append(':');
                write(sb, e.getValue());
            }
            sb.append('}');
        } else if (v instanceof Iterable) {
            sb.append('[');
            boolean first = true;
            for (Object o : (Iterable<?>) v) {
                if (!first) sb.append(',');
                first = false;
                write(sb, o);
            }
            sb.append(']');
        } else {
            quote(sb, v.toString());
        }
    }

    private static void quote(StringBuilder sb, String str) {
        sb.append('"');
        for (int k = 0; k < str.length(); k++) {
            char c = str.charAt(k);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> obj(Object v) {
        return v instanceof Map ? (Map<String, Object>) v : Collections.emptyMap();
    }

    static Map<String, Object> obj(Map<String, Object> m, String key) {
        return obj(m.get(key));
    }

    static boolean has(Map<String, Object> m, String key) {
        return m.get(key) != null;
    }

    @SuppressWarnings("unchecked")
    static List<Object> arr(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof List ? (List<Object>) v : Collections.emptyList();
    }

    static List<Map<String, Object>> objs(Map<String, Object> m, String key) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object o : arr(m, key)) out.add(obj(o));
        return out;
    }

    static List<String> strs(Map<String, Object> m, String key) {
        List<String> out = new ArrayList<>();
        for (Object o : arr(m, key)) out.add(o == null ? null : asString(o));
        return Collections.unmodifiableList(out);
    }

    static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : asString(v);
    }

    static String str(Map<String, Object> m, String key, String fallback) {
        String v = str(m, key);
        return v == null ? fallback : v;
    }

    private static String asString(Object v) {
        if (v instanceof String) return (String) v;
        if (v instanceof Double) {
            double d = (Double) v;
            if (d == Math.rint(d) && Math.abs(d) < 1e15) return Long.toString((long) d);
        }
        if (v instanceof Map || v instanceof List) return write(v);
        return v.toString();
    }

    static Long lngOrNull(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).longValue();
        if (v instanceof String) {
            try {
                return new BigDecimal(((String) v).trim()).longValue();
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    static long lng(Map<String, Object> m, String key) {
        Long v = lngOrNull(m, key);
        return v == null ? 0L : v;
    }

    static int integer(Map<String, Object> m, String key) {
        return (int) lng(m, key);
    }

    static Double dblOrNull(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof String) {
            try {
                return Double.parseDouble(((String) v).trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    static double dbl(Map<String, Object> m, String key) {
        Double v = dblOrNull(m, key);
        return v == null ? 0.0 : v;
    }

    static boolean bool(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof String) return Boolean.parseBoolean((String) v);
        if (v instanceof Number) return ((Number) v).doubleValue() != 0;
        return false;
    }
}
