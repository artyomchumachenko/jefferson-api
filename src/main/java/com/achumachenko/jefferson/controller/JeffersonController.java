package com.achumachenko.jefferson.controller;

import com.achumachenko.jefferson.model.dto.JeffersonRequest;
import com.achumachenko.jefferson.model.dto.JeffersonResponse;
import com.achumachenko.jefferson.model.dto.verbose.VerboseResponse;
import com.achumachenko.jefferson.service.JeffersonService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jefferson")
@CrossOrigin(origins = "*")
public class JeffersonController {

    private final JeffersonService service;

    public JeffersonController(JeffersonService service) {
        this.service = service;
    }

    @PostMapping(value = "/encrypt", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JeffersonResponse encrypt(@RequestBody JeffersonRequest req) {
        String cipher = service.encrypt(req.message(), req.key());
        return new JeffersonResponse(cipher);
    }

    @PostMapping(value = "/decrypt", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public JeffersonResponse decrypt(@RequestBody JeffersonRequest req) {
        String plain = service.decrypt(req.message(), req.key());
        return new JeffersonResponse(plain);
    }

    @PostMapping(value = "/encrypt/verbose",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public VerboseResponse encryptVerbose(@RequestBody JeffersonRequest req) {
        return service.encryptVerbose(req.message(), req.key());
    }

    @PostMapping(value = "/decrypt/verbose",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public VerboseResponse decryptVerbose(@RequestBody JeffersonRequest req) {
        return service.decryptVerbose(req.message(), req.key());
    }
}
