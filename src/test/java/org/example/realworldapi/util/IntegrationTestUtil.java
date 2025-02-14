package org.example.realworldapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.example.realworldapi.application.web.model.request.NewArticleRequest;
import org.example.realworldapi.infrastructure.provider.JwtTokenProvider;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.ArticleEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.CommentEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FavoriteRelationshipEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FavoriteRelationshipEntityKey;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FollowRelationshipEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.FollowRelationshipEntityKey;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagRelationshipEntity;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.TagRelationshipEntityKey;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.UserEntity;

@ApplicationScoped
public class IntegrationTestUtil {

  @Inject protected ObjectMapper objectMapper;
  @Inject protected JwtTokenProvider tokenProvider;
  @Inject protected Slugify slugify;
  @Inject EntityManager em;

  @Transactional
  public UserEntity createUserEntity(
      String username, String email, String bio, String image, String password) {
    final var userEntity = UserEntityUtils.create(username, email, password, bio, image);
    this.em.persist(userEntity);
    return userEntity;
  }

  public String token(UserEntity userEntity) {
    return tokenProvider.createUserToken(userEntity.getId().toString());
  }

  public String createUserToken(String str) {
    return tokenProvider.createUserToken(str);
  }

  @Transactional
  public void follow(UserEntity currentUser, UserEntity... followers) {
    final var user = this.em.find(UserEntity.class, currentUser.getId());

    for (UserEntity follower : followers) {
      FollowRelationshipEntityKey key = new FollowRelationshipEntityKey();
      key.setUser(user);
      key.setFollowed(follower);

      FollowRelationshipEntity followRelationshipEntity = new FollowRelationshipEntity();
      followRelationshipEntity.setPrimaryKey(key);
      this.em.persist(followRelationshipEntity);
    }

    this.em.persist(user);
  }

  @Transactional
  public TagEntity createTagEntity(String name) {
    final var tag = new TagEntity();
    tag.setId(UUID.randomUUID());
    tag.setName(name);
    this.em.persist(tag);
    return tag;
  }

  @Transactional
  public List<TagRelationshipEntity> createArticlesTags(
      List<ArticleEntity> articles, TagEntity... tags) {
    final var resultList = new LinkedList<TagRelationshipEntity>();

    for (ArticleEntity article : articles) {

      final var managedArticle = this.em.find(ArticleEntity.class, article.getId());

      for (TagEntity tag : tags) {
        final var managedTag = this.em.find(TagEntity.class, tag.getId());

        final var articlesTagsEntityKey = new TagRelationshipEntityKey();
        articlesTagsEntityKey.setArticle(managedArticle);
        articlesTagsEntityKey.setTag(managedTag);

        final var articlesTagsEntity = new TagRelationshipEntity();
        articlesTagsEntity.setPrimaryKey(articlesTagsEntityKey);

        this.em.persist(articlesTagsEntity);
        resultList.add(articlesTagsEntity);
      }
    }

    return resultList;
  }

  @Transactional
  public List<ArticleEntity> createArticles(
      UserEntity author, String title, String description, String body, int quantity) {
    final var articles = new LinkedList<ArticleEntity>();
    for (int articleIndex = 0; articleIndex < quantity; articleIndex++) {
      articles.add(
          createArticleEntity(
              author,
              title + "_" + articleIndex,
              description + "_" + articleIndex,
              body + "_" + articleIndex));
    }
    return articles;
  }

  public NewArticleRequest createNewArticle(
      String title, String description, String body, String... tagList) {
    NewArticleRequest newArticleRequest = new NewArticleRequest();
    newArticleRequest.setTitle(title);
    newArticleRequest.setDescription(description);
    newArticleRequest.setBody(body);
    newArticleRequest.setTagList(Arrays.asList(tagList));
    return newArticleRequest;
  }

  @Transactional
  public ArticleEntity createArticleEntity(
      UserEntity author, String title, String description, String body) {
    final var article = new ArticleEntity();
    article.setId(UUID.randomUUID());
    article.setTitle(title);
    article.setSlug(slugify.slugify(title));
    article.setDescription(description);
    article.setBody(body);
    article.setAuthor(author);
    this.em.persist(article);
    return article;
  }

  @Transactional
  public ArticleEntity findArticleEntityById(UUID id) {
    return this.em.find(ArticleEntity.class, id);
  }

  @Transactional
  public CommentEntity findCommentEntityById(UUID id) {
    return this.em.find(CommentEntity.class, id);
  }

  @Transactional
  public FavoriteRelationshipEntity favorite(ArticleEntity article, UserEntity user) {
    final var favoriteRelationshipEntity = favoriteRelationshipEntity(article, user);
    this.em.persist(favoriteRelationshipEntity);
    return favoriteRelationshipEntity;
  }

  @Transactional
  public CommentEntity createComment(UserEntity author, ArticleEntity article, String body) {
    final var comment = new CommentEntity();
    comment.setId(UUID.randomUUID());
    comment.setBody(body);
    comment.setArticle(article);
    comment.setAuthor(author);
    this.em.persist(comment);
    return comment;
  }

  FavoriteRelationshipEntity favoriteRelationshipEntity(
      ArticleEntity article, UserEntity loggedUser) {
    final var favoriteRelationshipEntityKey = favoriteRelationshipEntityKey(article, loggedUser);
    final var favoriteRelationshipEntity = new FavoriteRelationshipEntity();
    favoriteRelationshipEntity.setPrimaryKey(favoriteRelationshipEntityKey);
    return favoriteRelationshipEntity;
  }

  FavoriteRelationshipEntityKey favoriteRelationshipEntityKey(
      ArticleEntity article, UserEntity loggedUser) {
    final var favoriteRelationshipEntityKey = new FavoriteRelationshipEntityKey();
    favoriteRelationshipEntityKey.setArticle(article);
    favoriteRelationshipEntityKey.setUser(loggedUser);
    return favoriteRelationshipEntityKey;
  }
}
