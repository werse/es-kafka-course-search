package com.epam.ekc.search.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.CloseableThreadContext.Instance;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Work implements Identifiable {

  private static final long serialVersionUID = 1L;

  private String id;

  private String type;

  private String genre;

  private String title;

  private Set<Agent> agents = new HashSet<>();

  private Set<Instance> instances = new HashSet<>();
}
