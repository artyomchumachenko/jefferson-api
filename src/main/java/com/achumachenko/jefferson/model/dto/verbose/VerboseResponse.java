package com.achumachenko.jefferson.model.dto.verbose;

import java.util.List;

public record VerboseResponse(
        String cipher,
        List<DiskLogDto> disks
) {}
