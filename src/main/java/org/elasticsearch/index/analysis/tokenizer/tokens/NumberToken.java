package org.elasticsearch.index.analysis.tokenizer.tokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NumberToken extends Token {
    protected String sign;

    public NumberToken(String term, String sign, int offset, int position) {
        Term.Type type = sign.isEmpty() ? Term.Type.NUM : Term.Type.SIGNED_NUM;
        this.term = new Term(sign + term, offset, position, type);
        this.sign = sign;
    }

    public NumberToken(String term, int offset, int position) {
        this(term, "", offset, position);
    }

    public int getValue() {
        return Integer.parseInt(term.value());
    }

    @Override
    public List<Term> getSingleTerms() {
        int value = getValue();
        List<Term> parts = new ArrayList<>();
        parts.add(term);
        if (!sign.isEmpty() && value > 0) {
            parts.add(new Term(
                    String.valueOf(value),
                    term.getOffsetStart(),
                    term.getPosition(),
                    Term.Type.NUM
            ));
        }
        return parts;

    }

    @Override
    public List<Term> getComplexTerms() {
        return new ArrayList<>(Collections.singletonList(term));
    }

    @Override
    public List<Term> getTerms() {
        List<Term> terms = new ArrayList<>();
        terms.addAll(getSingleTerms());
        return terms;
    }

    public String getSign() {
        return this.sign;
    }
}
