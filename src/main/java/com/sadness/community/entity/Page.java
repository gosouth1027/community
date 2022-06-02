package com.sadness.community.entity;

/**
 * @version 1.0
 * @Date 2022/6/1 15:46
 * @Author SadAndBeautiful
 */
public class Page {

    //当前页码
    private int current = 1;
    //每页显示帖子数量
    private int limit = 10;
    //帖子总数量
    private int rows;
    //路径
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current > 0) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit > 0 && limit < 50) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页起始行数
     * @return
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotalPage() {
        return rows % limit == 0 ? rows / limit : rows / limit + 1;
    }

    /**
     * 获取前置索引页
     * @return
     */
    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取后置索引页
     * @return
     */
    public int getTo() {
        int to = current + 2;
        return to > getTotalPage() ? getTotalPage() : to;
    }


}
