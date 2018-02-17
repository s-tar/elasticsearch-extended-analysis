package org.elasticsearch.index.analysis.tokenizer.tokens;

public class Term {
    public enum Type {
        ALPHA,
        NUM,
        SIGNED_NUM,
        DECIMAL,
        SIGNED_DECIMAL,
        SYMBOL,
        COMPLEX,
    }
    private String term;

    private int offset;
    private int position;
    private Type type;
    public Term(String term, int offset, int position, Type type) {
        this.term = term;
        this.offset = offset;
        this.position = position;
        this.type = type;
    }

    public String value() {
        return term;
    }

    public Type getType() {
        return type;
    }

    public int getOffsetStart() {
        return offset;
    }

    public int getOffsetEnd() {
        return offset + term.length();
    }

    public int getPosition() {
        return position;
    }

    public boolean equals(Term term) {
        return this.term.equals(term.value());
    }

    public boolean equals(String term) {
        return this.term.equals(term);
    }

    @Override
    public String toString() {
        return String.format(
            "%s(start: %s; end: %s; position: %s)",
            this.term, this.getOffsetStart(),this.getOffsetEnd(), this.getPosition()
        );
    }
}
