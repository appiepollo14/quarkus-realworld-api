package org.example.realworldapi.integration;

import static io.restassured.RestAssured.given;
import static org.example.realworldapi.constants.TestConstants.API_PREFIX;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.example.realworldapi.util.IntegrationTestUtil;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestTransaction
public class TagsResourceIntegrationTest {

  @Inject IntegrationTestUtil integrationTestUtil;

  private final String TAGS_PATH = API_PREFIX + "/tags";

  @Test
  public void givenExistentTags_whenExecuteGetTagsEndpoint_shouldReturnTagListWithStatusCode200() {

    final var tag1 = integrationTestUtil.createTagEntity("tag 1");
    final var tag2 = integrationTestUtil.createTagEntity("tag 2");
    final var tag3 = integrationTestUtil.createTagEntity("tag 3");
    final var tag4 = integrationTestUtil.createTagEntity("tag 4");

    given()
        .contentType(MediaType.APPLICATION_JSON)
        .get(TAGS_PATH)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(
            "tags.size()",
            is(4),
            "tags",
            hasItems(tag1.getName(), tag2.getName(), tag3.getName(), tag4.getName()));
  }
}
