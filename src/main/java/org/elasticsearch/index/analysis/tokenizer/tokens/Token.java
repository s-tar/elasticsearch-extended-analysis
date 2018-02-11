package org.elasticsearch.index.analysis.tokenizer.tokens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Token {
    protected String term;

    public List<String> getSimpleTerms() {
        return Collections.singletonList(this.getTerm());
    }

    public List<String> getComplexTerms() {
        return new ArrayList<>(Collections.singletonList(this.term));
    }

    public List<String> getTerms() {
        ArrayList<String> terms = new ArrayList<>();
        terms.add(this.term);
        return terms;
    }

    public String getTerm() {
        return term;
    }

    @Override
    public String toString() {
        return String.format(
            "%s(term: %s)",
            this.getClass().getSimpleName(), this.term
        );
    }
}
