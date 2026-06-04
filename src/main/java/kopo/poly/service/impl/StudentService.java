package kopo.poly.service.impl;

import kopo.poly.dto.StudentDTO;
import kopo.poly.service.IStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mapper.IStudentMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor //
@Service
@MapperScan(basePackages = "mapper")//
public class StudentService implements IStudentService {

    private final IStudentMapper studentMapper;

    @Override
    public List<StudentDTO> insertStudent(StudentDTO pDTO) throws Exception {
        String className = this.getClass().getName();
        log.info("{}.insertStudent Start!", className);

        // 중복 체크 로직
        Optional<StudentDTO> res = Optional.ofNullable(studentMapper.getStudent(pDTO));
        if (res.isEmpty()) {
            studentMapper.insertStudent(pDTO);
            log.info("학생 등록 완료 - ID: {}", pDTO.getUserId());
        } else {
            log.warn("학생 등록 실패 - 이미 존재하는 ID: {}", pDTO.getUserId());
        }

        // 전체 목록 조회
        List<StudentDTO> rList = Optional.ofNullable(
                studentMapper.getStudentList()
        ).orElseGet(ArrayList::new);

        log.info("{}.insertStudent End!", className);
        return rList;
    }

    @Override
    public List<StudentDTO> deleteStudent(StudentDTO pDTO) throws Exception {
        return List.of();
    }
}