package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Vehicle;
import edu.ucsb.cs156.example.repositories.VehicleRepository;

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

@WebMvcTest(controllers = VehicleController.class)
@Import(TestConfig.class)
public class VehicleControllerTests extends ControllerTestCase {

        @MockBean
        VehicleRepository vehicleRepository;

        @MockBean
        UserRepository userRepository;

    

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/vehicle/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/vehicle/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/vehicle?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

      
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/vehicle/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/vehicle/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

                Vehicle vehicle = Vehicle.builder()
                                .brand("Cadillac")
                                .model("Escalade")
                                .licence("OG1")
                                .year("2023")
                                .build();

                when(vehicleRepository.findById(eq(7L))).thenReturn(Optional.of(vehicle));

                // act
                MvcResult response = mockMvc.perform(get("/api/vehicle?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(vehicleRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(vehicle);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(vehicleRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/vehicle?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(vehicleRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Vehicle with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_vehicle() throws Exception {

                // arrange

                Vehicle vehicle1 = Vehicle.builder()
                                .brand("Cadillac")
                                .model("Escalade")
                                .licence("OG1")
                                .year("2023")
                                .build();
                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                Vehicle vehicle2 = Vehicle.builder()
                                .brand("BMW")
                                .model("X1")
                                .licence("B3B3")
                                .year("2009")
                                .build();

                ArrayList<Vehicle> expectedVehicle = new ArrayList<>();
                expectedVehicle.addAll(Arrays.asList(vehicle1, vehicle2));

                when(vehicleRepository.findAll()).thenReturn(expectedVehicle);

                // act
                MvcResult response = mockMvc.perform(get("/api/vehicle/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(vehicleRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedVehicle);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_vehicle() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Vehicle vehicle1 = Vehicle.builder()
                                .brand("Cadillac")
                                .model("Escalade")
                                .licence("OG1")
                                .year("2023")
                                .build();

                when(vehicleRepository.save(eq(vehicle1))).thenReturn(vehicle1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/vehicle/post?brand=Cadillac&model=Escalade&licence=OG1&year=2023")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(vehicleRepository, times(1)).save(vehicle1);
                String expectedJson = mapper.writeValueAsString(vehicle1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Vehicle vehicle1 = Vehicle.builder()
                                .brand("Cadillac")
                                .model("Escalade")
                                .licence("OG1")
                                .year("2023")
                                .build();

                when(vehicleRepository.findById(eq(15L))).thenReturn(Optional.of(vehicle1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/vehicle?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(vehicleRepository, times(1)).findById(15L);
                verify(vehicleRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Vehicle with id 15 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_vehicle_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(vehicleRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/vehicle?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(vehicleRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Vehicle with id 15 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_vehicle() throws Exception {
                // arrange

                Vehicle vehicleOrig = Vehicle.builder()
                                .brand("Cadillac")
                                .model("Escalade")
                                .licence("OG1")
                                .year("2023")
                                .build();

                Vehicle vehicleEdited = Vehicle.builder()
                                .brand("BMW")
                                .model("X1")
                                .licence("B3B3")
                                .year("2009")
                                .build();

                String requestBody = mapper.writeValueAsString(vehicleEdited);

                when(vehicleRepository.findById(eq(67L))).thenReturn(Optional.of(vehicleOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/vehicle?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(vehicleRepository, times(1)).findById(67L);
                verify(vehicleRepository, times(1)).save(vehicleEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_vehicle_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

                Vehicle ucsbEditedDate = Vehicle.builder()
                                .brand("Cadillac")
                                .model("Escalade")
                                .licence("OG1")
                                .year("2023")
                                .build();


                String requestBody = mapper.writeValueAsString(ucsbEditedDate);

                when(vehicleRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/vehicle?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(vehicleRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Vehicle with id 67 not found", json.get("message"));

        }
}