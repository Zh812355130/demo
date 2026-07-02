package org.huan.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.huan.demo.core.BloomFilter;
import org.huan.demo.entity.Student;
import org.huan.demo.mapper.StudentMapper;
import org.huan.demo.service.StudentService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    public static final String CACHE_KEY ="student:";

    @Resource
    private StudentMapper studentMapper;
    @Resource
    private RedisTemplate<Object,Object> redisTemplate;
    @Resource
    private BloomFilter bloomFilter;

    @Override
    public List<Student> getAllStudent() {
        return list();
    }

    @Override
    public void addStudent(Student student) {
        boolean save = save(student);
        if(save){
            Student s = getById(student.getId());
            String key = CACHE_KEY+s.getId();
            redisTemplate.opsForValue().set(key,s);
        }
    }

    @Override
    public Student getStudent(Integer id) {
        String key = CACHE_KEY+id;
        //bloom filter
        if(!bloomFilter.checkKey(key)){
            log.info("==key:{}，不存在~~",key);
            return null;
        }
        Student s = (Student) redisTemplate.opsForValue().get(key);
        if(s == null){
            s = getById(id);
            if(s!=null){
                redisTemplate.opsForValue().set(key,s);
            }
        }
        return s;
    }
}
