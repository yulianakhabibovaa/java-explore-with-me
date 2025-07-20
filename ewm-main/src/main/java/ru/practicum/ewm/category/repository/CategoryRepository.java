package ru.practicum.ewm.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.category.model.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.category.id = :categoryId")
    boolean hasEvents(@Param("categoryId") Long categoryId);

    @Query(value = "SELECT * FROM categories ORDER BY id OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<Category> findCategoriesWithOffset(@Param("offset") int offset, @Param("limit") int limit);
}
