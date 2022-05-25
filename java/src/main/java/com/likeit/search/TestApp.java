package com.likeit.search;

public class TestApp {
    static class Father {

    }

    static class Son extends Father {

    }

    static public Son fSon() {
        return new Son();
    }

    public static void main(String[] args) {
        Father f1 = new Father();
        Father f2 = new Son();
        Father f3 = fSon();
    }
}
