package com.hyperlocal.server.Data;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * Wrapper class for Search Result
 * @author Diksha, Onish
 * @version 1.0
 * @since 1.0
 */
@AutoValue
public abstract class SearchSnippet implements Serializable {
  @JsonProperty public abstract ShopDetails shopDetails();
  @JsonProperty public abstract List<String> matchedPhrases();

  public static SearchSnippet create(ShopDetails shopDetails, List<String> matchedPhrases) {
    return new AutoValue_SearchSnippet(shopDetails, matchedPhrases);
  }
}
