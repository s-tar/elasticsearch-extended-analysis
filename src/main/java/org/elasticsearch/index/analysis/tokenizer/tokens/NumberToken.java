package org.elasticsearch.index.analysis.tokenizer.tokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NumberToken extends Token {
    private int value;
    protected String sign;

    public NumberToken(String term, String sign) {
        this.term = sign + term;
        this.sign = sign;
    }

    public NumberToken(String term) {
        this(term, "");
    }

    @Override
    public List<String> getSimpleTerms() {
        return new ArrayList<>(Collections.singletonList(term));
    }

    @Override
    public List<String> getComplexTerms() {
        int value = Integer.parseInt(term);
        List<String> parts = new ArrayList<>();
        parts.add(term);
        if (!sign.isEmpty() && value > 0) {
            parts.add(String.valueOf(value));
        }
        return parts;
    }

    @Override
    public List<String> getTerms() {
        List<String> terms = new ArrayList<>();
        terms.addAll(getSimpleTerms());
        terms.addAll(getComplexTerms());
        return terms;
    }

    public String getSign() {
        return this.sign;
    }
}
