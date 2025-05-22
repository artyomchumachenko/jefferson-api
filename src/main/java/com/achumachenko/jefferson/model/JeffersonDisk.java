package com.achumachenko.jefferson.model;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "jefferson_disk")
@Getter @Setter @NoArgsConstructor
public class JeffersonDisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int charIndex;          // позиция символа
    private String lang;            // EN / RU
    private String permutation;     // переставленный алфавит

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id")
    private JeffersonOperation operation;

    public JeffersonDisk(int charIndex, String lang, char[] permutation) {
        this.charIndex   = charIndex;
        this.lang        = lang;
        this.permutation = new String(permutation);
    }
}
