package kopo.poly.service;

import kopo.poly.dto.StudentDTO;
import java.util.List;

public interface IStudentService {
    // ⭐️ 여기 insertStudent의 'S'를 대문자로 똑같이 맞춰주어야 합니다!
    List<StudentDTO> insertStudent(StudentDTO pDTO) throws Exception;
    List<StudentDTO> deleteStudent(StudentDTO pDTO) throws Exception;
}