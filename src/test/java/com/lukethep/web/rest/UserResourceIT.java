package com.lukethep.web.rest;

import static com.lukethep.domain.UserAsserts.*;
import static com.lukethep.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukethep.IntegrationTest;
import com.lukethep.domain.User;
import com.lukethep.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link UserResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class UserResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_EMAIL = "AAAAAAAAAA";
    private static final String UPDATED_EMAIL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/users";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc restUserMockMvc;

    private User user;

    private User insertedUser;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static User createEntity() {
        return new User().name(DEFAULT_NAME).email(DEFAULT_EMAIL);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static User createUpdatedEntity() {
        return new User().name(UPDATED_NAME).email(UPDATED_EMAIL);
    }

    @BeforeEach
    public void initTest() {
        user = createEntity();
    }

    @AfterEach
    public void cleanup() {
        if (insertedUser != null) {
            userRepository.delete(insertedUser);
            insertedUser = null;
        }
    }

    @Test
    void createUser() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the User
        var returnedUser = om.readValue(
            restUserMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(user)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            User.class
        );

        // Validate the User in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        assertUserUpdatableFieldsEquals(returnedUser, getPersistedUser(returnedUser));

        insertedUser = returnedUser;
    }

    @Test
    void createUserWithExistingId() throws Exception {
        // Create the User with an existing ID
        user.setId("existing_id");

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(user)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        user.setName(null);

        // Create the User, which fails.

        restUserMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(user)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEmailIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        user.setEmail(null);

        // Create the User, which fails.

        restUserMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(user)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllUsers() throws Exception {
        // Initialize the database
        insertedUser = userRepository.save(user);

        // Get all the userList
        restUserMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(user.getId())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)));
    }

    @Test
    void getUser() throws Exception {
        // Initialize the database
        insertedUser = userRepository.save(user);

        // Get the user
        restUserMockMvc
            .perform(get(ENTITY_API_URL_ID, user.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL));
    }

    @Test
    void getNonExistingUser() throws Exception {
        // Get the user
        restUserMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putExistingUser() throws Exception {
        // Initialize the database
        insertedUser = userRepository.save(user);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        updatedUser.name(UPDATED_NAME).email(UPDATED_EMAIL);

        restUserMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(updatedUser))
            )
            .andExpect(status().isOk());

        // Validate the User in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedUserToMatchAllProperties(updatedUser);
    }

    @Test
    void putNonExistingUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        user.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserMockMvc
            .perform(put(ENTITY_API_URL_ID, user.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(user)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        user.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(user))
            )
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        user.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(user)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the User in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateUserWithPatch() throws Exception {
        // Initialize the database
        insertedUser = userRepository.save(user);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the user using partial update
        User partialUpdatedUser = new User();
        partialUpdatedUser.setId(user.getId());

        partialUpdatedUser.name(UPDATED_NAME).email(UPDATED_EMAIL);

        restUserMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUser.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedUser))
            )
            .andExpect(status().isOk());

        // Validate the User in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUserUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedUser, user), getPersistedUser(user));
    }

    @Test
    void fullUpdateUserWithPatch() throws Exception {
        // Initialize the database
        insertedUser = userRepository.save(user);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the user using partial update
        User partialUpdatedUser = new User();
        partialUpdatedUser.setId(user.getId());

        partialUpdatedUser.name(UPDATED_NAME).email(UPDATED_EMAIL);

        restUserMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedUser.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedUser))
            )
            .andExpect(status().isOk());

        // Validate the User in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertUserUpdatableFieldsEquals(partialUpdatedUser, getPersistedUser(partialUpdatedUser));
    }

    @Test
    void patchNonExistingUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        user.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserMockMvc
            .perform(patch(ENTITY_API_URL_ID, user.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(user)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        user.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(user))
            )
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamUser() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        user.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restUserMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(user)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the User in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteUser() throws Exception {
        // Initialize the database
        insertedUser = userRepository.save(user);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the user
        restUserMockMvc
            .perform(delete(ENTITY_API_URL_ID, user.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return userRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected User getPersistedUser(User user) {
        return userRepository.findById(user.getId()).orElseThrow();
    }

    protected void assertPersistedUserToMatchAllProperties(User expectedUser) {
        assertUserAllPropertiesEquals(expectedUser, getPersistedUser(expectedUser));
    }

    protected void assertPersistedUserToMatchUpdatableProperties(User expectedUser) {
        assertUserAllUpdatablePropertiesEquals(expectedUser, getPersistedUser(expectedUser));
    }
}
