package com.sadness.community.controller;

import com.sadness.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0
 * @Date 2022/5/30 8:45
 * @Author SadAndBeautiful
 */
//@Controller
//@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @ResponseBody
    @RequestMapping("/data")
    public String getData() {
        return alphaService.find();
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello() {
        return "Hello Spring Boot.";
    }


    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        System.out.println(request.getSession());
        response.getWriter().write("hello");
    }

    @RequestMapping("/students")
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") String current,
            @RequestParam(name = "limit", required = false, defaultValue = "15") String limit
    ) {
        System.out.println("current= " + current);
        System.out.println("limit= " + limit);
        return "some student";
    }

    @RequestMapping("/student/{id}")
    @ResponseBody
    public String getStudent(
            @PathVariable("id") int id
    ){
        System.out.println(id);
        return "a student " + id;
    }

    @RequestMapping("/student")
    @ResponseBody
    public String addStudent(
            @RequestParam("name") String name,
            @RequestParam("age") int age

    ){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }


    @GetMapping("/hero")
    public String getHero(Model model){
        model.addAttribute("name", "赵云");
        model.addAttribute("age", "35");
        return "/demo/view";
    }

    @GetMapping("/emp")
    @ResponseBody
    public Map<String, Object> getEmp(){
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 25);
        map.put("salary", 8000.00);
        return map;
    }

    @GetMapping("/emps")
    @ResponseBody
    public List<Map<String, Object>> getEmps(){

        List<Map<String, Object>> maps = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 25);
        map.put("salary", 8000.00);
        maps.add(map);

        map = new HashMap<>();
        map.put("name", "李四");
        map.put("age", 30);
        map.put("salary", 18000.00);
        maps.add(map);

        map = new HashMap<>();
        map.put("name", "王五");
        map.put("age", 38);
        map.put("salary", 10000.00);
        maps.add(map);

        return maps;
    }



}
