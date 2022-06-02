package com.sadness.community;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @version 1.0
 * @Date 2022/5/31 16:06
 * @Author SadAndBeautiful
 */
public class sad {


    //定义全局变量保存结果
    List<List<Integer>> res = new LinkedList<>();
    LinkedList<Integer> temp = new LinkedList<>();

    public List<List<Integer>> subsets(int[] nums) {
        //res.add(new ArrayList(temp));
        backstracking(nums, 0);
        return res;
    }

    void backstracking(int[] nums, int index) {

        res.add(new ArrayList(temp));
        if (index == nums.length) {
            return;
        }
        for (int i = index; i < nums.length; i++) {
            temp.add(nums[i]);
            backstracking(nums, i + 1);
            temp.removeLast();
        }
    }


    @Test
    public void test(){
        int[] nums = new int[]{1,2,3};
        List<List<Integer>> subsets = subsets(nums);
    }
}
