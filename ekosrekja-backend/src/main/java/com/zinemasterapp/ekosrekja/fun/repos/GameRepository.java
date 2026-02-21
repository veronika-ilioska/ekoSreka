package com.ekosrekja.ekosrekjafullstack.fun.repos;

import com.ekosrekja.ekosrekjafullstack.fun.Difficulty;
import com.ekosrekja.ekosrekjafullstack.fun.entity.Game;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
    Page<Game> findByTitleContainingIgnoreCase(String q, Pageable p);

    Page<Game> findByDifficulty(Difficulty d, Pageable p);
}
