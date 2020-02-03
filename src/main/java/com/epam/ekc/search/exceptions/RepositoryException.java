package com.epam.ekc.search.exceptions;

import java.io.IOException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepositoryException extends RuntimeException {

  private final String message;

  private final IOException exception;
}
