package org.elasticsearch.index.analysis.tokenizer;



import org.elasticsearch.index.analysis.tokenizer.tokens.*;

import java.util.*;

public class TokenList implements Iterable<Token> {
    private List<Token> tokens;
    private int size = 0;

    public TokenList() {
        this.tokens = new ArrayList<>();
    }

    public void add(Token token) {
        if (size < tokens.size()) {
            tokens.set(size, token);
        } else {
            tokens.add(token);
        }
        size++;
    }

    public Token get(int index) {
        return get(index, false);
    }

    private Token getDirect(int index) {
        return get(index, true);
    }

    private Token get(int index, boolean direct) {
        int size = direct ? tokens.size() : this.size;
        if (index >= 0) {
            return index < size ? tokens.get(index) : null;
        }
        return null;
    }

    public int size() {
        return this.size;
    }

    private void setSize(int size) {
        this.size = size > 0 ? size : 0;
    }

    private Token createComplex(int startIndex, int endIndex) {
        if (endIndex - startIndex == 1) {
            return getDirect(startIndex);
        } else {
            List<Token> tokens = new ArrayList<>(this.tokens.subList(startIndex, endIndex));
            Token first = tokens.get(0);
            int offset = first.term().getOffsetStart();
            int position = first.term().getPosition();
            return new ComplexToken(tokens, offset, position);
        }
    }

    public void complicateTokens() {
        if (size == 0) return;
        int originSize = size;
        int prevIndex = -1;
        int index = 0;
        int nextIndex = 1;
        int prevSlashIndex = 0;
        int termStartIndex = 0;
        setSize(0);
        while (index <= originSize) {
            if (isSignedNumber(index, nextIndex)) {
                Term sign = getDirect(index).term();
                Term number = getDirect(nextIndex++).term();
                int offset = sign.getOffsetStart();
                int position = sign.getPosition();
                tokens.set(index, new NumberToken(number.value(), sign.value(), offset, position));
            }

            if (isFloat(prevIndex , index, nextIndex)) {
                NumberToken number = (NumberToken) getDirect(index);
                Term floatingPoint = getDirect(nextIndex++).term();
                Term fraction = getDirect(nextIndex++).term();
                String floatTerm = Math.abs(number.getValue()) + floatingPoint.value() + fraction.value();
                String sign = number.getSign();
                int offset = number.term().getOffsetStart();
                int position = number.term().getPosition();
                tokens.set(index, new FloatToken(floatTerm, sign, offset, position));
            }

            Token token = getDirect(index);
            Token prevToken = getDirect(prevIndex);
            if (token instanceof SymbolToken && prevToken == null) {
                token = null;
            }

            if (token instanceof SymbolToken && ((SymbolToken) token).isComma()) {
                token = null;
            }

            if (
                (token instanceof SymbolToken && ((SymbolToken) token).isSlash()) ||
                (token == null && termStartIndex < prevSlashIndex)
            ) {
                int currentIndex = size;
                setSize(prevSlashIndex);
                if (prevSlashIndex < currentIndex) {
                    add(createComplex(prevSlashIndex, currentIndex));
                }
                prevSlashIndex = size + 1;
            }

            if (token == null) {
                int currentIndex = size;
                if (termStartIndex < currentIndex) {
                    setSize(termStartIndex);
                    add(createComplex(termStartIndex, currentIndex));
                    if (nextIndex < originSize) {
                        add(null);
                    }
                    prevSlashIndex = termStartIndex = size;
                }
            } else {
                add(token);
            }
            prevIndex = size - 1;
            index = nextIndex;
            nextIndex++;
        }
    }

    private boolean isSignedNumber(int index, int nextIndex) {
        Token token = getDirect(index);
        if (token instanceof SymbolToken && ((SymbolToken) token).isSign()) {
            Token nextToken = getDirect(nextIndex);
            if (nextToken instanceof NumberToken) {
                Token prevToken = getDirect(index - 1);
                return prevToken == null || prevToken instanceof SymbolToken;
            }
        }
        return false;
    }

    private boolean isFloat(int prevIndex, int index, int nextIndex) {
        Token number = getDirect(index);
        Token floatingPoint = getDirect(nextIndex);
        Token fraction = getDirect(nextIndex + 1);
        if (
            number instanceof NumberToken &&
            fraction instanceof NumberToken &&
            floatingPoint instanceof SymbolToken && ((SymbolToken) floatingPoint).isFloatingPoint()
        ) {
            Token prevToken = getDirect(prevIndex);
            Token nextToken = getDirect(nextIndex + 2);
            Token possibleNumber = getDirect(nextIndex + 3);
            boolean isPrevTokenValid = (
                prevToken == null ||
                (prevToken instanceof SymbolToken && !prevToken.term().equals(floatingPoint.term()))
            );
            boolean isNextTokensValid = (
                nextToken == null ||
                !nextToken.term().equals(floatingPoint.term()) &&
                !(possibleNumber instanceof NumberToken && nextToken.term().equals("."))
            );
            return isPrevTokenValid && isNextTokensValid;
        }
        return false;
    }

    public Iterator<Token> iterator() {
        return new Iterator<Token>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size();
            }

            @Override
            public Token next() {
                return get(currentIndex++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append('[');
        for (int i = 0; i < size; i++) {
            if (i > 0) output.append(", ");
            output.append(tokens.get(i));
        }
        output.append(']');
        return output.toString();
    }
}
