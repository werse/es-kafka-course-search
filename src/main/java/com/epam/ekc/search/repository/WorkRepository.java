package com.epam.ekc.search.repository;

import com.epam.ekc.search.dto.Resource;
import com.epam.ekc.search.model.Work;
import org.springframework.stereotype.Repository;

@Repository
public class WorkRepository extends ElasticRepository<Work, Resource> {

}
