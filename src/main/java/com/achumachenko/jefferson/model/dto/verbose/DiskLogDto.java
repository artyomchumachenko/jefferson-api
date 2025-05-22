package com.achumachenko.jefferson.model.dto.verbose;

public record DiskLogDto(
        int    index,        // позиция символа в исходной строке
        String original,     // исходный символ
        String lang,         // EN | RU
        String permutation,  // перемешанный диск
        String result        // символ после замены
) {}
