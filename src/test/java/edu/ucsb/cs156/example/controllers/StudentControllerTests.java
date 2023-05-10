package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.repositories.StudentRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.entities.Student;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



@WebMvcTest(controllers = StudentController.class)
@Import(TestConfig.class)
public class StudentControllerTests extends ControllerTestCase  {
    @MockBean
    StudentRepository studentRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/students/admin/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/students/all")).andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles={"USER"})
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/students/all")).andExpect(status().is(200)); // logged out users can't get all
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/students/post")).andExpect(status().is(403));
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/students?id=1")).andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/students/post")).andExpect(status().is(403)); // only admins can post
    }

    // Tests with mocks for database actions
    @WithMockUser(roles ={"USER"})
    @Test
    public void logged_in_users_can_get_all_students() throws Exception {
        Student student1 = Student.builder()
                        .firstName("James")
                        .lastName("Harden")
                        .perm("12345")
                        .email("jHarden@ucsb.edu")
                        .phoneNumber("673-38385")
                        .major("Finance").build();
        
        Student student2 = Student.builder()
                        .firstName("Anthony")
                        .lastName("Davis")
                        .perm("67891")
                        .email("aDavis@ucsb.edu")
                        .phoneNumber("673-38386")
                        .major("Math").build();
        
        ArrayList<Student> expectedStudents = new ArrayList<>();
        expectedStudents.addAll(Arrays.asList(student1, student2));

        // fake response data for findAll method
        when(studentRepository.findAll()).thenReturn(expectedStudents);
        // act
        MvcResult response = mockMvc.perform(get("/api/students/all"))
                        .andExpect(status().isOk()).andReturn();

        // assert
        // check how many findAll method have been called
        verify(studentRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedStudents);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = {"ADMIN", "USER"})
    @Test
    public void an_admin_user_can_post_a_new_student() throws Exception {
        Student student1 = Student.builder()
                    .firstName("Tim")
                    .lastName("Cook")
                    .perm("42525")
                    .email("tCook@ucsb.edu")
                    .phoneNumber("423-86759")
                    .major("Business")
                    .build();
        
        when(studentRepository.save(eq(student1))).thenReturn(student1);

        MvcResult response = mockMvc.perform(post("/api/students/post?firstName=Tim&lastName=Cook&perm=42525&email=tCook@ucsb.edu&phoneNumber=423-86759&major=Business").with(csrf())).andExpect(status().isOk()).andReturn();

        // act
        verify(studentRepository, times(1)).save(student1);
        String expectedJson = mapper.writeValueAsString(student1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

        Student student = Student.builder()
                    .firstName("James")
                    .lastName("Harden")
                    .perm("111111")
                    .email("jHarden@uccb.edu")
                    .phoneNumber("666-000000")
                    .major("Business")
                    .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        // act
        MvcResult response = mockMvc.perform(get("/api/students?id=1"))
        .andExpect(status().isOk()).andReturn();

        verify(studentRepository, times(1)).findById(eq(1L));
        String expectedJson = mapper.writeValueAsString(student);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/students?id=1"))
                    .andExpect(status().isNotFound()).andReturn();
        
        // assert
        verify(studentRepository, times(1)).findById(eq(1L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("Student with id 1 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_student() throws Exception {
        Student studentOrig = Student.builder()
            .firstName("James")
            .lastName("Harden")
            .perm("12345")
            .email("jHarden@ucsb.edu")
            .phoneNumber("673-38385")
            .major("Finance").build();

        Student studentEdit = Student.builder()
                .firstName("Anthony")
                .lastName("Davis")
                .perm("67891")
                .email("aDavis@ucsb.edu")
                .phoneNumber("673-38386")
                .major("Math").build();
        
        String requestBody = mapper.writeValueAsString(studentEdit);
        when(studentRepository.findById(eq(2L))).thenReturn(Optional.of(studentOrig));

        // act
        MvcResult response = mockMvc.perform(
        put("/api/students?id=2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
                    .andExpect(status().isOk()).andReturn();
        
        // assert
        verify(studentRepository, times(1)).findById(2L);
        verify(studentRepository, times(1)).save(studentEdit); // should be saved with correct user
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_student_that_does_not_exist() throws Exception {
        Student studentEdit = Student.builder()
                .firstName("Anthony")
                .lastName("Davis")
                .perm("67891")
                .email("aDavis@ucsb.edu")
                .phoneNumber("673-38386")
                .major("Math").build();
        
        String requestBody = mapper.writeValueAsString(studentEdit);

        when(studentRepository.findById(eq(1L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
        put("/api/students?id=1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
                    .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(studentRepository, times(1)).findById(1L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("Student with id 1 not found", json.get("message"));
    }


}
