package com.example.demo.student;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository repository;

    public Integer uploadStudents(MultipartFile file) {
        Set<Student> students = parseCsv(file);
        repository.saveAll(students);

        return students.size();
    }

    public Resource downloadStudents() {
        List<Student> students = repository.findAll();
        return generateCsv(students);
    }

    private Resource generateCsv(List<Student> students) {

        File file = new File("students.csv");

        if (!file.exists()) file.mkdirs();


        try (FileWriter writer = new FileWriter(file)) {

            writer.append("firstname,lastname,age\n");

            for (Student student : students) {
                writer
                        .append(student.getName()).append(",")
                        .append(student.getSurname()).append(",")
                        .append(String.valueOf(student.getAge())).append("\n");
            }

            return new UrlResource(file.toURI());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Student> parseCsv(MultipartFile file) {

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            HeaderColumnNameMappingStrategy<StudentCsvRepresentation> strategy =
                    new HeaderColumnNameMappingStrategy<>();

            strategy.setType(StudentCsvRepresentation.class);

            CsvToBean<StudentCsvRepresentation> csvToBean =
                    new CsvToBeanBuilder<StudentCsvRepresentation>(reader)
                            .withMappingStrategy(strategy)
                            .withIgnoreEmptyLine(true)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build();

            return csvToBean.parse()
                    .stream()
                    .map(csvLine -> Student.builder()
                            .name(csvLine.getFname())
                            .surname(csvLine.getLname())
                            .age(csvLine.getAge())
                            .build()
                    )
                    .collect(Collectors.toSet());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
