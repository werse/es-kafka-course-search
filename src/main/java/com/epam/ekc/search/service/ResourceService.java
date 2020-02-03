package com.epam.ekc.search.service;

import com.epam.ekc.search.dto.Resource;
import com.epam.ekc.search.repository.WorkRepository;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
@RequiredArgsConstructor
public class ResourceService {

  private final WorkRepository workRepository;

  public List<Resource> getResources(String query, MultiValueMap<String, Object> facets, String customerId) {
    return workRepository.getResources(query, facets, customerId);
  }

  public List<String> autoComplete(String query, String customerId) throws IOException {
    return workRepository.autocomplete(query, customerId);
  }


}
