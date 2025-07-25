package ru.practicum.ewm.user.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.user.model.User;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsById(@NonNull Long id);

    List<User> findByIdIn(Collection<Long> ids);

    @Query(value = "SELECT * FROM users ORDER BY id OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<User> findUsersWithOffset(@Param("offset") int offset, @Param("limit") int limit);

    @Query(value = "SELECT * FROM users WHERE id IN :ids ORDER BY id OFFSET :offset LIMIT :limit", nativeQuery = true)
    List<User> findUsersByIdsWithOffset(@Param("ids") List<Long> ids, @Param("offset") int offset, @Param("limit") int limit);
}
