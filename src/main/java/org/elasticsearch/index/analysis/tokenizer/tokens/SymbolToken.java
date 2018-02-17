package org.elasticsearch.index.analysis.tokenizer.tokens;

import java.util.*;

public class SymbolToken extends Token {
    private static Set<Character> AVAILABLE = new HashSet<>(
        Arrays.asList('&', '%', '°', '>', '<', '*', '+', '-', '.', ',', '/', '\\')
    );

    private Set<Character> PREFIXES = new HashSet<>(
        Arrays.asList('%', '°', '+', '-')
    );

    public SymbolToken(String term, int offset, int position) {
        this.term = new Term(term, offset, position, Term.Type.SYMBOL);
    }

    public static boolean isAvailableSymbol(int charCode) {
        return (
            AVAILABLE.contains((char) charCode) ||
            Character.getType(charCode) == Character.CURRENCY_SYMBOL
        );
    }

    public boolean isComma() {
        return term.value().charAt(0) == ',';
    }

    public boolean isSign() {
        return "+".equals(term.value()) || "-".equals(term.value());
    }

    public boolean isFloatingPoint() {
        return ".".equals(term.value()) || ",".equals(term.value());
    }

    public boolean isSlash() {
        return term.value().charAt(0) == '/' || term.value().charAt(0) == '\\';
    }

    public boolean isPrefix() {
        return (
            PREFIXES.contains(term.value().charAt(0)) ||
            Character.getType(term.value().charAt(0)) == Character.CURRENCY_SYMBOL
        );
    }

    @Override
    public List<Term> getSingleTerms() {
        return new ArrayList<>();
    }
}
