package com.achumachenko.jefferson.repository;

import com.achumachenko.jefferson.model.JeffersonOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JeffersonOperationRepository extends JpaRepository<JeffersonOperation, UUID> {}
