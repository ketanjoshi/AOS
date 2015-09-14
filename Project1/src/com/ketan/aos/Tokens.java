package com.ketan.aos;

import java.util.ArrayList;

public class Tokens {

    private ArrayList<Token> tokens;

    public Tokens() {
        tokens = new ArrayList<>();
    }

    public ArrayList<Token> getTokenList() {
        return tokens;
    }

    public void addToken(Token token) {
        tokens.add(token);
    }

    public int size() {
        return tokens.size();
    }
}
