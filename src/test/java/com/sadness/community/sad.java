package com.sadness.community;

import com.sadness.community.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @version 1.0
 * @Date 2022/5/31 16:06
 * @Author SadAndBeautiful
 */
public class sad {

    @Test
    public void test() {

        int[][] obstacleGrid = new int[3][3];
        obstacleGrid[0] = new int[]{0, 0, 0};
        obstacleGrid[1] = new int[]{0, 1, 0};
        obstacleGrid[2] = new int[]{0, 0, 0};

        int m = obstacleGrid.length;
        int n = obstacleGrid[0].length;
        // 确定dp数组,dp[i][j]表示从起点到达[i][j]有几种路径
        int[][] dp = new int[m][n];
        // 初始化bp数组,首行有障碍位置及其右面位置置0,首列有有障碍位置及其下面位置置0，其余置1
        // 首列
        int flag = 1;
        for (int i = 0; i < m; i++) {
            if (obstacleGrid[i][0] == 1) {
                flag = 0;
            }
            dp[i][0] = flag;
        }
        // 首行
        flag = 1;
        for (int i = 0; i < n; i++) {
            if (obstacleGrid[0][i] == 1) {
                flag = 0;
            }
            dp[0][i] = flag;
        }

        // 遍历顺序从左到右,遇到障碍置0,其余位置dp[i][j] = dp[i-1][j] + dp[i][j-1]
        for (int i = 1; i < m; i++) {
            for (int j = 1; j < n; j++) {
                if (obstacleGrid[i][j] == 1) {
                    dp[i][j] = 0;
                } else {
                    dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
                }
            }
        }
        System.out.println("11");

    }
}
