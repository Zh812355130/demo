package org.huan.demo.controller;

import jakarta.annotation.Resource;
import org.huan.demo.entity.Student;
import org.huan.demo.mapper.StudentMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mysql")
public class MysqlTestController
{
    @Resource
    private StudentMapper studentMapper;

    @GetMapping("/student")
    public @ResponseBody List<Student> getStudentList(){
        return studentMapper.selectList(null);
    }

}
