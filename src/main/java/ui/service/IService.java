package ui.service;

import java.util.List;

/**
 * Generic Service Interface for CRUD operations.
 * Uses Generic Programming with type parameter T.
 *
 * @param <T> The entity type
 */
public interface IService<T> {

    /**
     * Add a new entity to the database
     * @param entity The entity to add
     * @return true if successful, false otherwise
     */
    boolean add(T entity);

    /**
     * Update an existing entity
     * @param entity The entity to update
     * @return true if successful, false otherwise
     */
    boolean update(T entity);

    /**
     * Delete an entity by ID
     * @param id The entity ID
     * @return true if successful, false otherwise
     */
    boolean delete(int id);

    /**
     * Get an entity by ID
     * @param id The entity ID
     * @return The entity or null if not found
     */
    T getById(int id);

    /**
     * Get all entities
     * @return List of all entities
     */
    List<T> getAll();
}
