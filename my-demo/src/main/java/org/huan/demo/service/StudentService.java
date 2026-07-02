package org.huan.demo.service;


import org.huan.demo.entity.Student;

import java.util.List;

public interface StudentService {

    List<Student> getAllStudent();

    void addStudent(Student student);

    Student getStudent(Integer id);

}
