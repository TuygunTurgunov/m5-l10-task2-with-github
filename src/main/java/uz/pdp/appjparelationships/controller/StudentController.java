package uz.pdp.appjparelationships.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import uz.pdp.appjparelationships.entity.Address;
import uz.pdp.appjparelationships.entity.Group;
import uz.pdp.appjparelationships.entity.Student;
import uz.pdp.appjparelationships.entity.Subject;
import uz.pdp.appjparelationships.payload.StudentDto;
import uz.pdp.appjparelationships.repository.AddressRepository;
import uz.pdp.appjparelationships.repository.GroupRepository;
import uz.pdp.appjparelationships.repository.StudentRepository;
import uz.pdp.appjparelationships.repository.SubjectRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/student")
public class StudentController {
    @Autowired
    StudentRepository studentRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    AddressRepository addressRepository;





    @PostMapping
    public  String addStudent(@RequestBody StudentDto studentDto){
        Optional<Group> optionalGroup = groupRepository.findById(studentDto.getGroupId());
        if (!optionalGroup.isPresent())
            return "group not found by group id";

        boolean exists = studentRepository.existsByFirstNameAndLastNameAndGroupId(studentDto.getFirstName(), studentDto.getLastName(), studentDto.getGroupId());
        if (exists)
            return "This student is already exists in this group";
        Address address = new Address();
        address.setCity(studentDto.getCity());
        address.setDistrict(studentDto.getDistrict());
        address.setStreet(studentDto.getStreet());
        Address savedAddress = addressRepository.save(address);

        List<Integer> subjects = studentDto.getSubjects();
        List<Subject> subjectList = subjectRepository.findAllById(subjects);

        Student student=new Student();
        student.setFirstName(studentDto.getFirstName());
        student.setLastName(studentDto.getLastName());
        student.setGroup(optionalGroup.get());
        student.setSubjects(subjectList);
        student.setAddress(savedAddress);
        studentRepository.save(student);
        return "student saved";
    }
    @PutMapping("/{id}")
    public String editStudent(@PathVariable Integer id, @RequestBody StudentDto studentDto){
        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (!optionalStudent.isPresent())
            return "student not found by id";

        Optional<Group> optionalGroup = groupRepository.findById(studentDto.getGroupId());
        if (!optionalGroup.isPresent())
            return "group not fond by id";

        List<Subject> subjectList = subjectRepository.findAllById(studentDto.getSubjects());


        Student student = optionalStudent.get();
        Address address = student.getAddress();
        address.setStreet(studentDto.getStreet());
        address.setDistrict(studentDto.getDistrict());
        address.setCity(studentDto.getCity());
        addressRepository.save(address);
        student.setGroup(optionalGroup.get());
        student.setSubjects(subjectList);
        student.setFirstName(studentDto.getFirstName());
        student.setLastName(studentDto.getLastName());
        studentRepository.save(student);
        return "student edited";
    }
    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable Integer id){
        try {
            studentRepository.deleteById(id);
            return "student deleted";
        }catch (Exception e){
            return "Error in deleting";
        }
    }





    //1. VAZIRLIK
    @GetMapping("/forMinistry")
    public Page<Student> getStudentListForMinistry(@RequestParam int page) {
        //1-1=0     2-1=1    3-1=2    4-1=3
        //select * from student limit 10 offset (0*10)
        //select * from student limit 10 offset (1*10)
        //select * from student limit 10 offset (2*10)
        //select * from student limit 10 offset (3*10)
        Pageable pageable = PageRequest.of(page, 10);
        Page<Student> studentPage = studentRepository.findAll(pageable);
        return studentPage;
    }

    //2. UNIVERSITY
    @GetMapping("/forUniversity/{universityId}")
    public Page<Student> getStudentListForUniversity(@PathVariable Integer universityId,
                                                     @RequestParam int page) {
        //1-1=0     2-1=1    3-1=2    4-1=3
        //select * from student limit 10 offset (0*10)
        //select * from student limit 10 offset (1*10)
        //select * from student limit 10 offset (2*10)
        //select * from student limit 10 offset (3*10)
        Pageable pageable = PageRequest.of(page, 10);
        Page<Student> studentPage = studentRepository.findAllByGroup_Faculty_UniversityId(universityId, pageable);
        return studentPage;
    }

    //3. FACULTY DEKANAT
    @GetMapping("/forFaculty/{facultyId}")
    public Page<Student> getStudentsByFacultyId(@PathVariable Integer facultyId,
                                                @RequestParam Integer page){
        Pageable pageable=PageRequest.of(page,2);
        Page<Student> allByGroup_facultyId = studentRepository.findAllByGroup_FacultyId(facultyId, pageable);
        Page<Student> allByGroup_faculty_id = studentRepository.findAllByGroup_Faculty_Id(facultyId, pageable);
        return allByGroup_faculty_id;

    }

    //4. GROUP OWNER
    @GetMapping("/forGroup/{groupId}")
    public Page<Student> studentPageByGroupId(@PathVariable Integer groupId,@RequestParam Integer page){
        Pageable pageable=PageRequest.of(page,5);
        Page<Student> allByGroupId = studentRepository.findAllByGroupId(groupId, pageable);
        return allByGroupId;
    }

    //5.
    @GetMapping("/byStudentId/{studentId}")
    public Student getStudent(@PathVariable Integer studentId){
        Optional<Student> optionalStudent = studentRepository.findById(studentId);
        if (optionalStudent.isPresent())
            return optionalStudent.get();
        return null;

    }
}
