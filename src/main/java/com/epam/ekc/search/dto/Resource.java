package com.epam.ekc.search.dto;

import com.epam.ekc.search.model.Agent;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.CloseableThreadContext.Instance;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

  private String workId;

  private String workType;

  private String genre;

  private String title;

  private Set<Agent> agents;

  private Set<Instance> instances;

}
