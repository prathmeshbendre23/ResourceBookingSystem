package com.example.resourcebooking.service;

import com.example.resourcebooking.dto.request.ResourceRequest;
import com.example.resourcebooking.dto.response.ResourceResponse;

import java.util.List;

public interface ResourceService {

    ResourceResponse createResource(ResourceRequest request);

    List<ResourceResponse> getAllResources();

    ResourceResponse getResourceById(Long id);

    ResourceResponse updateResource(Long id, ResourceRequest request);

    void deleteResource(Long id);
}
