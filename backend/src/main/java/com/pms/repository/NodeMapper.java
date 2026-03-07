package com.pms.repository;

import org.neo4j.driver.types.Node;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Neo4j 节点属性到 Java 类型的转换。
 */
public final class NodeMapper {

    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    public static Long getLong(Node n, String key) {
        if (n == null || !n.containsKey(key)) return null;
        try {
            return n.get(key).asLong();
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getInt(Node n, String key) {
        if (n == null || !n.containsKey(key)) return null;
        try {
            return (int) n.get(key).asLong();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getString(Node n, String key) {
        if (n == null || !n.containsKey(key)) return null;
        try {
            return n.get(key).asString(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDateTime getDateTime(Node n, String key) {
        String s = getString(n, key);
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDateTime.parse(s, DATETIME);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public static LocalDate getDate(Node n, String key) {
        String s = getString(n, key);
        if (s == null || s.isEmpty()) return null;
        try {
            return LocalDate.parse(s, DATE);
        } catch (Exception e) {
            return null;
        }
    }

    public static String toDateTimeStr(LocalDateTime v) {
        return v == null ? null : v.format(DATETIME);
    }

    public static String toDateStr(LocalDate v) {
        return v == null ? null : v.format(DATE);
    }

    public static BigDecimal getBigDecimal(Node n, String key) {
        if (n == null || !n.containsKey(key)) return null;
        try {
            String s = n.get(key).asString(null);
            return s == null || s.isEmpty() ? null : new BigDecimal(s);
        } catch (Exception e) {
            try {
                return BigDecimal.valueOf(n.get(key).asDouble());
            } catch (Exception e2) {
                return null;
            }
        }
    }
}
