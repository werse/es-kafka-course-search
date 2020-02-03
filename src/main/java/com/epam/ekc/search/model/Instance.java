package com.epam.ekc.search.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Instance implements Identifiable {

  private static final long serialVersionUID = 1L;

  private String id;

  private String type;

  private String title;

  private String language;

  private Instant publicationDate;

  @JsonIgnoreProperties("instances")
  private Work work;
}
