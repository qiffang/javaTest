package com.fang.example.java;

import junit.framework.Assert;

/**
 * Created by andy on 4/4/16.
 */
public class Regular {
    public static boolean test(String expr, String text) {
        if (!expr.contains("*")) {
            return expr.length() == text.length() ? true : false;
        }
        int skip = -1, index = 0;
        for (char c : expr.toCharArray()) {
           switch (c) {
               case '*':
                   break;
               case '.':
                   index += 1;
                   skip += 1;
                   break;
               default:
                   skip += 1;
                   if ((index = _search(skip, text, c, index)) == -1)
                       return false;
           }
        }
        return true;
    }

    /**
     * the expr first char is *, skip 0
     * the expr first char is ., skip 1
     * the expr first char is *., skip 1
     * the expr first char is .*, skip 1
     * the expr first char is .., skip 2
     */
    private static int _search(int skip, String text, char a, int index) {
        switch (skip) {
            case -1:
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == a)
                        return i;
                }
                break;
            default:
                if (text.length() > index && text.charAt(index) == a)
                    return index;
        }

        return -1;
    }

    public static void main(String[]args) {
//        Assert.assertEquals(test("a*", "a"), true);
//        Assert.assertEquals(test("*a", "a"), true);
//        Assert.assertEquals(test("*.a", "a"), false);
//        Assert.assertEquals(test("*.b", "ab"), true);
        Assert.assertEquals(test("*.a.b..c", "cdadbccc"), true);
//        Assert.assertEquals(test(".a", "bba"), false);
    }
}
