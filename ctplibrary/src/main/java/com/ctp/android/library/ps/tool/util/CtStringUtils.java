package com.ctp.android.library.ps.tool.util;
public class CtStringUtils
{
    public static boolean isEmpty(String input)
    {
        return input == null || input.trim().length() <= 0;
    }
}
