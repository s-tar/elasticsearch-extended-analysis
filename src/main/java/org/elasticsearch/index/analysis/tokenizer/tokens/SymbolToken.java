package org.elasticsearch.index.analysis.tokenizer.tokens;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SymbolToken extends Token {
    private static Set<Character> AVAILABLE = new HashSet<>(
        Arrays.asList('&', '%', '°', '>', '<', '*', '+', '-', '.', ',', '/', '\\')
    );

    private Set<Character> PREFIXES = new HashSet<>(
        Arrays.asList('%', '°', '+', '-')
    );

    public SymbolToken(String term) {
        this.term = term;
    }

    public static boolean isAvailableSymbol(int charCode) {
        return (
            AVAILABLE.contains((char) charCode) ||
            Character.getType(charCode) == Character.CURRENCY_SYMBOL
        );
    }

    public boolean isSign() {
        return "+".equals(term) || "-".equals(term);
    }

    public boolean isFloatingPoint() {
        return ".".equals(term) || ",".equals(term);
    }

    public boolean isSlash() {
        return term.charAt(0) == '/';
    }

    public boolean isPrefix() {
        return (
            PREFIXES.contains(term.charAt(0)) ||
            Character.getType(term.charAt(0)) == Character.CURRENCY_SYMBOL
        );
    }
}
