package com.g2bulk;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

final class TestRunner {
    interface Body {
        void run() throws Exception;
    }

    private final List<String> names = new ArrayList<>();
    private final List<Body> bodies = new ArrayList<>();

    void test(String name, Body body) {
        names.add(name);
        bodies.add(body);
    }

    int run() {
        int passed = 0;
        int failed = 0;
        for (int k = 0; k < names.size(); k++) {
            long start = System.nanoTime();
            try {
                bodies.get(k).run();
                passed++;
                System.out.printf("PASS %s (%d ms)%n", names.get(k), (System.nanoTime() - start) / 1_000_000);
            } catch (Throwable t) {
                failed++;
                System.out.printf("FAIL %s: %s%n", names.get(k), t);
                StackTraceElement[] trace = t.getStackTrace();
                for (int j = 0; j < Math.min(trace.length, 6); j++) System.out.println("    at " + trace[j]);
            }
        }
        System.out.printf("%d passed, %d failed%n", passed, failed);
        return failed;
    }

    static void ok(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    static void ok(boolean condition) {
        ok(condition, "expected condition to hold");
    }

    static void eq(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    static <E extends Throwable> E raises(Class<E> type, Body body) {
        return raises(type, body, e -> true);
    }

    static <E extends Throwable> E raises(Class<E> type, Body body, Predicate<E> check) {
        try {
            body.run();
        } catch (Throwable t) {
            if (!type.isInstance(t)) throw new AssertionError("expected " + type.getSimpleName() + " but got " + t, t);
            E e = type.cast(t);
            if (!check.test(e)) throw new AssertionError("exception did not match: " + t, t);
            return e;
        }
        throw new AssertionError("expected " + type.getSimpleName() + " to be thrown");
    }
}
