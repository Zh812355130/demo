package org.huan.demo.controller;

import jakarta.annotation.Resource;
import org.huan.demo.entity.Student;
import org.huan.demo.service.StudentService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/student")
public class StudentController {
    @Resource
    private StudentService studentService;


    @PostMapping
    public String addStudent(@RequestBody Student student) {
        studentService.addStudent(student);
        return "success";
    }

    @GetMapping("/{id}")
    public Student getById(@PathVariable Integer id){
        return studentService.getStudent(id);
    }


}
