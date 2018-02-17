package org.elasticsearch.index.analysis.tokenizer.tokens;

import java.util.ArrayList;
import java.util.List;

public class FloatToken extends NumberToken {
    private float value;
    private Term dotFormattedTerm;
    private Term commaFormattedTerm;

    public FloatToken(String term, int offset, int position) {
        this(term, "", offset, position);
    }

    public FloatToken(String term, String sign, int offset, int position) {
        super(term, sign, offset, position);
        Term.Type type = sign.isEmpty() ? Term.Type.DECIMAL : Term.Type.SIGNED_DECIMAL;
        this.term = new Term(this.term.value(), offset, position, type);
        this.dotFormattedTerm = new Term(this.term.value().replace(',', '.'), offset, position, type);
        this.commaFormattedTerm = new Term(this.term.value().replace('.', ','), offset, position, type);
        this.value = Float.parseFloat(this.dotFormattedTerm.value());
    }

    @Override
    public List<Term> getSingleTerms() {
        List<Term> parts = new ArrayList<>();
        parts.add(
            new Term(
                String.valueOf((int) value),
                term.getOffsetStart(),
                term.getPosition(),
                value > 0 ? Term.Type.NUM : Term.Type.SIGNED_NUM
            )
        );

        if (!sign.isEmpty() && value > 0) {
            parts.add(new Term(
                String.valueOf(value),
                term.getOffsetStart(),
                this.term.getPosition(),
                Term.Type.DECIMAL
            ));

            parts.add(new Term(
                String.valueOf(value).replace('.',','),
                term.getOffsetStart(),
                this.term.getPosition(),
                Term.Type.DECIMAL
            ));
        }
        parts.add(dotFormattedTerm);
        parts.add(commaFormattedTerm);

        return parts;
    }

    @Override
    public List<Term> getComplexTerms() {
        List<Term> parts = new ArrayList<>();
        parts.add(this.term);
        return parts;
    }

    @Override
    public List<Term> getTerms() {
        List<Term> terms = new ArrayList<>();
        terms.addAll(getSingleTerms());
        return terms;
    }


}
