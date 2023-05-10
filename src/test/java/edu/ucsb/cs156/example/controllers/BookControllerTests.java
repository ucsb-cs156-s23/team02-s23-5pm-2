package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Book;
import edu.ucsb.cs156.example.repositories.BookRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = BookController.class)
@Import(TestConfig.class)
public class BookControllerTests extends ControllerTestCase {

        @MockBean
        BookRepository bookRepository;

        @MockBean
        UserRepository userRepository;

    

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/book/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/book/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/book?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

      
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/book/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/book/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                Book book = Book.builder()
                                .title("HP")
                                .author("jk")
                                .date("2023")
                                .build();

                when(bookRepository.findById(eq(7L))).thenReturn(Optional.of(book));

                // act
                MvcResult response = mockMvc.perform(get("/api/book?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(bookRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(book);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(bookRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/book?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(bookRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Book with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_book() throws Exception {

                // arrange

                Book book1 = Book.builder()
                                .title("test")
                                .author("test")
                                .date("2021")
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                Book book2 = Book.builder()
                                .title("test1")
                                .author("test1")
                                .date("2022")
                                .build();

                ArrayList<Book> expectedBook = new ArrayList<>();
                expectedBook.addAll(Arrays.asList(book1, book2));

                when(bookRepository.findAll()).thenReturn(expectedBook);

                // act
                MvcResult response = mockMvc.perform(get("/api/book/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(bookRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedBook);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_book() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Book book1 = Book.builder()
                                .title("firstDayOfClasses")
                                .author("2022")
                                .date("2023")
                                .build();

                when(bookRepository.save(eq(book1))).thenReturn(book1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/book/post?title=firstDayOfClasses&author=2022&date=2023")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(bookRepository, times(1)).save(book1);
                String expectedJson = mapper.writeValueAsString(book1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Book book1 = Book.builder()
                                .title("firstDayOfClasses")
                                .author("20222")
                                .date("2023")
                                .build();

                when(bookRepository.findById(eq(15L))).thenReturn(Optional.of(book1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/book?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(bookRepository, times(1)).findById(15L);
                verify(bookRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Book with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_book_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(bookRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/book?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(bookRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Book with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_book() throws Exception {
                // arrange

                Book bookOrig = Book.builder()
                                .title("firstDayOfClasses")
                                .author("20222")
                                .date("2023")
                                .build();

                Book bookEdited = Book.builder()
                                .title("firstDayOfFestivus")
                                .author("20232")
                                .date("2023")
                                .build();

                String requestBody = mapper.writeValueAsString(bookEdited);

                when(bookRepository.findById(eq(67L))).thenReturn(Optional.of(bookOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/book?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(bookRepository, times(1)).findById(67L);
                verify(bookRepository, times(1)).save(bookEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_book_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Book ucsbEditedDate = Book.builder()
                                .title("firstDayOfClasses")
                                .author("20222")
                                .date("2023")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbEditedDate);

                when(bookRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/book?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(bookRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Book with id 67 not found", json.get("message"));

        }
}
