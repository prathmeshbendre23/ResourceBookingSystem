package com.example.resourcebooking.service.impl;

import com.example.resourcebooking.dto.request.ResourceRequest;
import com.example.resourcebooking.dto.response.ResourceResponse;
import com.example.resourcebooking.entity.Resource;
import com.example.resourcebooking.exception.ResourceNotFoundException;
import com.example.resourcebooking.repository.ResourceRepository;
import com.example.resourcebooking.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ResourceRepository resourceRepository;

    @Override
    @Transactional
    public ResourceResponse createResource(ResourceRequest request) {
        log.info("Creating new resource: {}", request.getName());

        Resource resource = Resource.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .type(request.getType().trim())
                .price(request.getPrice())
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .build();

        Resource saved = resourceRepository.save(resource);
        log.info("Resource created successfully with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources() {
        log.debug("Fetching all resources");
        return resourceRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        log.debug("Fetching resource with ID: {}", id);
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        return mapToResponse(resource);
    }

    @Override
    @Transactional
    public ResourceResponse updateResource(Long id, ResourceRequest request) {
        log.info("Updating resource with ID: {}", id);
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        resource.setName(request.getName().trim());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType().trim());
        resource.setPrice(request.getPrice());
        resource.setAvailable(request.getAvailable());

        Resource updated = resourceRepository.save(resource);
        log.info("Resource ID {} updated successfully", updated.getId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteResource(Long id) {
        log.info("Deleting resource with ID: {}", id);
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException(id);
        }
        resourceRepository.deleteById(id);
        log.info("Resource ID {} deleted successfully", id);
    }

    private ResourceResponse mapToResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .type(resource.getType())
                .price(resource.getPrice())
                .available(resource.getAvailable())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
