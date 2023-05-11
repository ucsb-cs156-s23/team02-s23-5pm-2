package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.EnergyDrink;
import edu.ucsb.cs156.example.repositories.EnergyDrinkRepository;

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

@WebMvcTest(controllers = EnergyDrinkController.class)
@Import(TestConfig.class)
public class EnergyDrinkControllerTests extends ControllerTestCase {

        @MockBean
        EnergyDrinkRepository energydrinkRepository;

        @MockBean
        UserRepository userRepository;

    

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/energydrink/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/energydrink/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/energydrink?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

      
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/energydrink/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/energydrink/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                EnergyDrink energydrink = EnergyDrink.builder()
                                .name("Red Bull")
                                .flavor("Peach")
                                .calories("150")
                                .build();

                when(energydrinkRepository.findById(eq(7L))).thenReturn(Optional.of(energydrink));

                // act
                MvcResult response = mockMvc.perform(get("/api/energydrink?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(energydrinkRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(energydrink);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(energydrinkRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/energydrink?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(energydrinkRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Energy drink with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_energydrink() throws Exception {

                // arrange

                EnergyDrink energydrink1 = EnergyDrink.builder()
                                .name("Red Bull")
                                .flavor("Peach")
                                .calories("150")
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                EnergyDrink energydrink2 = EnergyDrink.builder()
                                .name("Monster")
                                .flavor("Mango")
                                .calories("170")
                                .build();

                ArrayList<EnergyDrink> expectedEnergyDrink = new ArrayList<>();
                expectedEnergyDrink.addAll(Arrays.asList(energydrink1, energydrink2));

                when(energydrinkRepository.findAll()).thenReturn(expectedEnergyDrink);

                // act
                MvcResult response = mockMvc.perform(get("/api/energydrink/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(energydrinkRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedEnergyDrink);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_energydrink() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                EnergyDrink energydrink1 = EnergyDrink.builder()
                                .name("Red Bull")
                                .flavor("Peach")
                                .calories("150")
                                .build();

                when(energydrinkRepository.save(eq(energydrink1))).thenReturn(energydrink1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/energydrink/post?name=Red Bull&flavor=Peach&calories=150")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(energydrinkRepository, times(1)).save(energydrink1);
                String expectedJson = mapper.writeValueAsString(energydrink1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_calories() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                EnergyDrink energydrink1 = EnergyDrink.builder()
                                .name("Red Bull")
                                .flavor("Peach")
                                .calories("150")
                                .build();

                when(energydrinkRepository.findById(eq(15L))).thenReturn(Optional.of(energydrink1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/energydrink?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(energydrinkRepository, times(1)).findById(15L);
                verify(energydrinkRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("EnergyDrink with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_energydrink_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(energydrinkRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/energydrink?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(energydrinkRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("EnergyDrink with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_energydrink() throws Exception {
                // arrange

                EnergyDrink energydrinkOrig = EnergyDrink.builder()
                                .name("Red Bull")
                                .flavor("Peach")
                                .calories("150")
                                .build();

                EnergyDrink energydrinkEdited = EnergyDrink.builder()
                                .name("Monster")
                                .flavor("Mango")
                                .calories("170")
                                .build();

                String requestBody = mapper.writeValueAsString(energydrinkEdited);

                when(energydrinkRepository.findById(eq(67L))).thenReturn(Optional.of(energydrinkOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/energydrink?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(energydrinkRepository, times(1)).findById(67L);
                verify(energydrinkRepository, times(1)).save(energydrinkEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_energydrink_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                EnergyDrink ucsbEditedDate = EnergyDrink.builder()
                                .name("Red Bull")
                                .flavor("Peach")
                                .calories("150")
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbEditedDate);

                when(energydrinkRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/energydrink?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(energydrinkRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("EnergyDrink with id 67 not found", json.get("message"));

        }
}
