package org.elasticsearch.index.analysis.tokenizer.tokens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FloatToken extends NumberToken {
    private float value;
    private String dotFormatted;
    private String commaFormatted;

    public FloatToken(String term, String sign) {
        super(term);
        this.sign = sign;
        this.dotFormatted = this.term.replace(',', '.');
        this.commaFormatted = this.term.replace('.', ',');
        this.value = Float.parseFloat(this.dotFormatted);

    }

    public FloatToken(String term) {
        super(term, "");
    }

    @Override
    public List<String> getSimpleTerms() {
        return new ArrayList<>(Collections.singletonList(term));
    }

    @Override
    public List<String> getComplexTerms() {
        List<String> parts = new ArrayList<>();
        parts.add(String.valueOf((int) value));
        if (!sign.isEmpty() && value > 0) {
            parts.add(String.valueOf(value));
        }
        if (!term.equals(dotFormatted)) {
            parts.add(dotFormatted);
        }

        if (!term.equals(commaFormatted)) {
            parts.add(commaFormatted);
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


}
