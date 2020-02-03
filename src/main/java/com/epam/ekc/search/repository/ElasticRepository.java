package com.epam.ekc.search.repository;

import static java.lang.String.valueOf;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

import com.epam.ekc.search.exceptions.RepositoryException;
import com.epam.ekc.search.model.Identifiable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.client.util.Resources;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.PostConstruct;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.MultiValueMap;

public abstract class ElasticRepository<E extends Identifiable, D> {

  @Setter(onMethod = @__(@Autowired))
  protected RestHighLevelClient client;

  @Setter(onMethod = @__(@Autowired))
  protected ObjectMapper objectMapper;

  private static String DID_YOU_MEAN_QUERY;

  public void init() {
    DID_YOU_MEAN_QUERY = Resources.getResource("didyoumean.json").toString();
  }

  protected Class<E> getEntityClass() {
    return (Class<E>) (Objects.requireNonNull(GenericTypeResolver
        .resolveTypeArguments(getClass(), ElasticRepository.class))[0]);
  }

  protected Class<D> getDtoClass() {
    return (Class<D>) (Objects.requireNonNull(GenericTypeResolver
        .resolveTypeArguments(getClass(), ElasticRepository.class))[1]);
  }


  @PostConstruct
  public void initIndex() throws IOException {
    CreateIndexRequest request = new CreateIndexRequest(getIndexName());
    if (!client.indices().exists(new GetIndexRequest(getIndexName()), RequestOptions.DEFAULT)) {
      client.indices().create(request, RequestOptions.DEFAULT);
    }
  }

  public E saveOrUpdate(E obj) {
    try {
      IndexRequest indexRequest = new IndexRequest(getIndexName())
          .id(valueOf(obj.getId()))
          .source(convertObjToString(obj), XContentType.JSON);

      IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
      return obj;
    } catch (IOException e) {
      throw new RepositoryException("Cannot save: " + obj.getClass().getSimpleName(), e);
    }
  }

  public Optional<D> findById(Object id) throws Exception {

    GetRequest getRequest = new GetRequest(getIndexName(), valueOf(id));

    GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
    Map<String, Object> resultMap = getResponse.getSource();

    return Optional.ofNullable(convertMapToObj(resultMap));

  }

  public List<D> findAll() throws Exception {

    SearchRequest searchRequest = buildSearchRequest();
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(matchAllQuery());
    searchRequest.source(searchSourceBuilder);

    SearchResponse searchResponse =
        client.search(searchRequest, RequestOptions.DEFAULT);

    return getSearchResult(searchResponse);
  }


  public List<D> findByQuery(QueryBuilder queryBuilder, String customerId) {
    try {
      SearchResponse searchResponse = executeSearchQuery(queryBuilder, customerId);

      return getSearchResult(searchResponse);
    } catch (IOException e) {
      throw new RepositoryException("Cannot find by query: " + queryBuilder.toString(), e);
    }
  }

  private SearchResponse executeSearchQuery(QueryBuilder queryBuilder, String customerId)
      throws IOException {
    SearchRequest searchRequest = buildSearchRequest();

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    searchSourceBuilder.query(queryBuilder);

    searchRequest.source(searchSourceBuilder);

    searchRequest.routing(customerId);

    return client.search(searchRequest, RequestOptions.DEFAULT);
  }


  public String deleteObj(String id) throws Exception {

    DeleteRequest deleteRequest = new DeleteRequest(getIndexName(), id);
    DeleteResponse response = client.delete(deleteRequest, RequestOptions.DEFAULT);
    return response.getResult().name();

  }

  protected String getIndexName() {
    return getEntityClass().getSimpleName().toLowerCase();
  }

  protected String convertObjToString(E obj) throws JsonProcessingException {
    return objectMapper.writeValueAsString(obj);
  }

  protected D convertMapToObj(Map<String, Object> map) {
    return objectMapper.convertValue(map, getDtoClass());
  }

  private List<D> getSearchResult(SearchResponse response) {

    SearchHit[] searchHit = response.getHits().getHits();

    List<D> objs = new ArrayList<>();

    for (SearchHit hit : searchHit) {
      objs.add(convertMapToObj(hit.getSourceAsMap()));
    }

    return objs;
  }

  private SearchRequest buildSearchRequest() {

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.indices(getIndexName());
    return searchRequest;
  }

  public List<String> autocomplete(String query, String customerId) throws IOException {
    SearchRequest searchRequest = new SearchRequest("music");
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    SuggestionBuilder termSuggestionBuilder = SuggestBuilders.completionSuggestion("suggest")
        .prefix(query);
    SuggestBuilder suggestBuilder = new SuggestBuilder();
    suggestBuilder.addSuggestion("suggest", termSuggestionBuilder);
    searchSourceBuilder.suggest(suggestBuilder);

    searchRequest.source(searchSourceBuilder);

    Suggest suggest = client.search(searchRequest, RequestOptions.DEFAULT).getSuggest();
    CompletionSuggestion entries = suggest.getSuggestion("suggest");

    return StreamSupport.stream(entries.spliterator(), false)
        .flatMap(entry -> entry.getOptions().stream()).map(option -> option.getText().string())
        .collect(Collectors.toList());
  }

  public HttpEntity getDidYouMean(String query, String customerId) throws IOException {
    Request request = new Request("GET", "/_search");
    request.setJsonEntity(DID_YOU_MEAN_QUERY);
    return client.getLowLevelClient().performRequest(request).getEntity();
  }


  public List<D> getResources(String query,
      MultiValueMap<String, Object> facets, String customerId) {
    QueryBuilder queryBuilder = boolQuery()
        .filter(termQuery("customerId", customerId))
        .filter(getFacetsFilter(facets))
        .should(matchQuery("title", query).fuzziness(2));
    return findByQuery(queryBuilder, customerId);
  }

  public BoolQueryBuilder getFacetsFilter(MultiValueMap<String, Object> values) {
    BoolQueryBuilder bool = boolQuery();
    values.forEach((key, value) -> bool.must(termsQuery(key, value)));
    return bool;
  }
}
