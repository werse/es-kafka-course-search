package com.epam.ekc.search.controller;

import com.epam.ekc.search.dto.Resource;
import com.epam.ekc.search.service.ResourceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/resources")
public class ResourceController {

  private final ResourceService service;

  @GetMapping
  public List<Resource> getResources(@RequestParam MultiValueMap<String, Object> params,
      @RequestParam String customerId) {
    String query = String.valueOf(params.remove("query").get(0));
    return service.getResources(query, params, customerId);
  }

}
