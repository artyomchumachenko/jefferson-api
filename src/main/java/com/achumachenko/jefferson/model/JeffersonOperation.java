package com.achumachenko.jefferson.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.*;

import com.achumachenko.jefferson.model.constant.OperationMode;

@Entity
@Table(name = "jefferson_operation")
@Getter @Setter @NoArgsConstructor
public class JeffersonOperation {

    @Id
    private UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    private OperationMode mode;

    @Column(name = "key_value")
    private String key;

    @Column(columnDefinition = "text")
    private String inputMessage;

    @Column(columnDefinition = "text")
    private String outputMessage;

    private OffsetDateTime createdAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JeffersonDisk> disks = new ArrayList<>();
}
