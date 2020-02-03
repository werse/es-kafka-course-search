package com.epam.ekc.search.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@AllArgsConstructor(staticName = "of")
public class Agent implements Identifiable {

  private String id;

  private String firstName;

  private String lastName;

  @JsonIgnoreProperties("agents")
  private Work work;
}
