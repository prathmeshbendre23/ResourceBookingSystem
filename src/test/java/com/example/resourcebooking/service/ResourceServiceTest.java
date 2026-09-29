package com.example.resourcebooking.service;

import com.example.resourcebooking.dto.request.ResourceRequest;
import com.example.resourcebooking.dto.response.ResourceResponse;
import com.example.resourcebooking.entity.Resource;
import com.example.resourcebooking.exception.ResourceNotFoundException;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.service.impl.ResourceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceServiceImpl resourceService;

    private Resource resource;

    @BeforeEach
    void setUp() {
        resource = Resource.builder()
                .id(1L)
                .name("Conference Room A")
                .description("Spacious meeting room")
                .type("Room")
                .price(new BigDecimal("50.00"))
                .available(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create resource successfully")
    void testCreateResource() {
        ResourceRequest request = ResourceRequest.builder()
                .name("Conference Room A")
                .description("Spacious meeting room")
                .type("Room")
                .price(new BigDecimal("50.00"))
                .available(true)
                .build();

        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);

        ResourceResponse response = resourceService.createResource(request);

        assertNotNull(response);
        assertEquals("Conference Room A", response.getName());
        assertEquals(new BigDecimal("50.00"), response.getPrice());
        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    @DisplayName("Should get all resources")
    void testGetAllResources() {
        when(resourceRepository.findAll()).thenReturn(List.of(resource));

        List<ResourceResponse> responses = resourceService.getAllResources();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Conference Room A", responses.get(0).getName());
    }

    @Test
    @DisplayName("Should get resource by ID")
    void testGetResourceById() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));

        ResourceResponse response = resourceService.getResourceById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when resource ID does not exist")
    void testGetResourceByIdNotFound() {
        when(resourceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                resourceService.getResourceById(999L)
        );
    }

    @Test
    @DisplayName("Should update resource successfully")
    void testUpdateResource() {
        ResourceRequest updateRequest = ResourceRequest.builder()
                .name("Updated Room A")
                .description("Renovated room")
                .type("Room")
                .price(new BigDecimal("65.00"))
                .available(false)
                .build();

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(resource));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(i -> i.getArgument(0));

        ResourceResponse updated = resourceService.updateResource(1L, updateRequest);

        assertEquals("Updated Room A", updated.getName());
        assertEquals(new BigDecimal("65.00"), updated.getPrice());
        assertFalse(updated.getAvailable());
    }

    @Test
    @DisplayName("Should delete resource when ID exists")
    void testDeleteResource() {
        when(resourceRepository.existsById(1L)).thenReturn(true);
        doNothing().when(resourceRepository).deleteById(1L);

        assertDoesNotThrow(() -> resourceService.deleteResource(1L));
        verify(resourceRepository, times(1)).deleteById(1L);
    }
}
