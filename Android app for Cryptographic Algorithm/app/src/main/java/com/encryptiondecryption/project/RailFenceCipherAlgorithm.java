package com.encryptiondecryption.project;

import android.util.Log;

public class RailFenceCipherAlgorithm {

    public static String railFenceEncryption(int depth, String plainText) throws Exception {

        int r = depth, len = plainText.length();
        int c;
         c = (len/ depth)+1;
/*
        if(len % 2 == 0)
        {
            plainText +=" ";
            len = plainText.length();
            c = len/ depth;
        }
        else
            c=(len/depth)+1;
*/
        char mat[][] = new char[r][c];
        int k = 0;

        String cipherText = "";

        for (int i = 0; i < c; i++) {
            for (int j = 0; j < r; j++) {
                if (k != len)
                    mat[j][i] = plainText.charAt(k++);
                else
                    mat[j][i] = 'X';
            }
        }
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                cipherText += mat[i][j];
            }
        }
        Log.d("cipherTextRailfence", cipherText + " ");
        return cipherText;
    }

    public static  String railFenceDecryption(int depth, String cipherText) {
        int r = depth, len = cipherText.length();
        int c = len / depth;
        char mat[][] = new char[r][c];
        int k = 0;

        String plainText = "";

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                mat[i][j] = cipherText.charAt(k++);
            }
        }
        for (int i = 0; i < c; i++) {
            for (int j = 0; j < r; j++) {
                    if(mat[j][i] != 'X')
                        plainText += mat[j][i];
            }
        }

        return plainText;
    }


}
