package org.shunya.kb.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SynonymWordRepository extends JpaRepository<SynonymWord, Long> {
    List<SynonymWord> findAllByWordsContainingIgnoreCase(String match);
}
