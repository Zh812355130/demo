package org.huan.demo;

import java.util.*;

public class LeetCodeTest {
    public static void main(String[] args) {
        Solution s = new Solution();
        System.out.println(Arrays.toString(s.plusOne(new int[]{8, 8, 9})));

    }
}

class Solution {
    public int[] plusOne(int[] digits) {
        int last_index = digits.length-1;
        while (last_index > -1){
            if(digits[last_index] + 1 <10){
                break;
            }
            last_index-- ;
        }
        if(last_index == -1){
            int[]  r = new int[digits.length+1];
            r[0] = 1;
            return r;
        }
        int[]  r = new int[digits.length];
        System.arraycopy(digits,0,r,0,last_index);
        r[last_index] = digits[last_index]+1;
        return r;
    }
}