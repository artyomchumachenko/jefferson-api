package com.achumachenko.jefferson.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.achumachenko.jefferson.model.JeffersonDisk;
import com.achumachenko.jefferson.model.JeffersonOperation;
import com.achumachenko.jefferson.model.constant.OperationMode;
import com.achumachenko.jefferson.model.dto.verbose.DiskLogDto;
import com.achumachenko.jefferson.model.dto.verbose.VerboseResponse;
import com.achumachenko.jefferson.repository.JeffersonOperationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class JeffersonService {

    private final JeffersonOperationRepository opRepo;

    private static final char[] EN_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] RU_ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".toCharArray();

    @Transactional
    public String encrypt(String message, String key) {
        log.debug("Called encrypt with key='{}', message length={} chars", key, message.length());
        String result = transformAndPersist(message, key, true);
        log.debug("Encrypt completed, output='{}'", result);
        return result;
    }

    @Transactional
    public String decrypt(String message, String key) {
        log.debug("Called decrypt with key='{}', message length={} chars", key, message.length());
        String result = transformAndPersist(message, key, false);
        log.debug("Decrypt completed, output='{}'", result);
        return result;
    }

    // ==================== PRIVATE ====================

    @Transactional
    protected String transformAndPersist(String message, String key, boolean encrypt) {
        log.debug("Starting {} operation for persistence", encrypt ? "ENCRYPT" : "DECRYPT");
        List<JeffersonDisk> disks = new ArrayList<>();
        String result = transformInternal(message, key, encrypt, disks);

        JeffersonOperation op = new JeffersonOperation();
        op.setMode(encrypt ? OperationMode.ENCRYPT : OperationMode.DECRYPT);
        op.setKey(key);
        op.setInputMessage(message);
        op.setOutputMessage(result);
        op.setCreatedAt(OffsetDateTime.now());
        disks.forEach(d -> d.setOperation(op));
        op.getDisks().addAll(disks);

        log.debug("Persisting operation with {} disks", disks.size());
        opRepo.save(op);
        log.debug("Operation persisted with ID={}", op.getId());

        return result;
    }

    /** Полная бизнес-логика + наполнение списка дисков. */
    private String transformInternal(
            String message,
            String key,
            boolean encrypt,
            List<JeffersonDisk> disks) {

        long seed = (key == null ? 0L : key.hashCode());
        Random rnd = new Random(seed);
        log.debug("Initialized Random with seed={}", seed);

        StringBuilder out = new StringBuilder(message.length());

        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            boolean lower = Character.isLowerCase(ch);
            char up = Character.toUpperCase(ch);

            char[] alpha;
            String lang;

            if (up >= 'A' && up <= 'Z') {
                alpha = EN_ALPHABET.clone();
                lang = "EN";
            } else if ((up >= 'А' && up <= 'Я') || up == 'Ё') {
                alpha = RU_ALPHABET.clone();
                lang = "RU";
            } else {
                out.append(ch);
                log.debug("Position {}: non-alphabetic '{}', copied as-is", i, ch);
                continue;
            }

            // Shuffle the disk
            for (int p = alpha.length - 1; p > 0; p--) {
                int j = rnd.nextInt(p + 1);
                char tmp = alpha[p];
                alpha[p] = alpha[j];
                alpha[j] = tmp;
            }
            log.debug("Position {}: {} disk after shuffle = {}", i, lang, new String(alpha));

            // Save disk entity
            disks.add(new JeffersonDisk(i, lang, alpha.clone()));

            int idx = indexOf(up, alpha);
            if (idx < 0) {
                out.append(ch);
                log.warn("Position {}: '{}' not found in shuffled alphabet", i, ch);
                continue;
            }

            int offset = encrypt ? 1 : -1;
            int newPos = (idx + offset + alpha.length) % alpha.length;
            char mapped = alpha[newPos];
            char finalCh = lower ? Character.toLowerCase(mapped) : mapped;
            out.append(finalCh);
            log.debug("Position {}: '{}' -> '{}' (idx {} -> {})", i, ch, finalCh, idx, newPos);
        }

        log.debug("transformInternal completed, output='{}'", out.toString());
        return out.toString();
    }

    private static int indexOf(char c, char[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == c) return i;
        }
        return -1;
    }

    @Transactional
    public VerboseResponse encryptVerbose(String msg, String key) {
        log.debug("Called encryptVerbose with key='{}', message length={} chars", key, msg.length());
        VerboseResponse resp = transformAndPersistVerbose(msg, key, true);
        log.debug("encryptVerbose completed with {} log entries", resp.disks().size());
        return resp;
    }

    @Transactional
    public VerboseResponse decryptVerbose(String msg, String key) {
        log.debug("Called decryptVerbose with key='{}', message length={} chars", key, msg.length());
        VerboseResponse resp = transformAndPersistVerbose(msg, key, false);
        log.debug("decryptVerbose completed with {} log entries", resp.disks().size());
        return resp;
    }

    @Transactional
    protected VerboseResponse transformAndPersistVerbose(
            String message,
            String key,
            boolean encrypt) {

        log.debug("Starting verbose {} operation", encrypt ? "ENCRYPT" : "DECRYPT");
        List<JeffersonDisk> entityDisks = new ArrayList<>();
        List<DiskLogDto> dtoDisks = new ArrayList<>();

        String cipher = transformVerboseInternal(message, key, encrypt, entityDisks, dtoDisks);
        log.debug("Verbose transformation result='{}'", cipher);

        JeffersonOperation op = new JeffersonOperation();
        op.setMode(encrypt ? OperationMode.ENCRYPT : OperationMode.DECRYPT);
        op.setKey(key);
        op.setInputMessage(message);
        op.setOutputMessage(cipher);
        op.setCreatedAt(OffsetDateTime.now());
        entityDisks.forEach(d -> d.setOperation(op));
        op.getDisks().addAll(entityDisks);

        log.debug("Persisting verbose operation with {} disks", entityDisks.size());
        opRepo.save(op);
        log.debug("Verbose operation persisted with ID={}", op.getId());

        return new VerboseResponse(cipher, dtoDisks);
    }

    /** Алгоритм + формирование двух списков: entityDisks — для БД, dtoDisks — для фронта */
    private String transformVerboseInternal(
            String message,
            String key,
            boolean encrypt,
            List<JeffersonDisk> entityDisks,
            List<DiskLogDto> dtoDisks) {

        long seed = (key == null ? 0L : key.hashCode());
        Random rnd = new Random(seed);
        log.debug("Initialized Random for verbose with seed={}", seed);
        StringBuilder out = new StringBuilder(message.length());

        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            boolean lower = Character.isLowerCase(ch);
            char up = Character.toUpperCase(ch);

            char[] alpha;
            String lang;

            if (up >= 'A' && up <= 'Z') {
                alpha = EN_ALPHABET.clone();
                lang = "EN";
            } else if ((up >= 'А' && up <= 'Я') || up == 'Ё') {
                alpha = RU_ALPHABET.clone();
                lang = "RU";
            } else {
                out.append(ch);
                log.debug("Verbose pos {}: non-alphabetic '{}', copied as-is", i, ch);
                continue;
            }

            for (int p = alpha.length - 1; p > 0; p--) {
                int j = rnd.nextInt(p + 1);
                char tmp = alpha[p];
                alpha[p] = alpha[j];
                alpha[j] = tmp;
            }
            log.debug("Verbose pos {}: {} disk shuffle = {}", i, lang, new String(alpha));

            entityDisks.add(new JeffersonDisk(i, lang, alpha.clone()));

            int idx = indexOf(up, alpha);
            if (idx < 0) {
                out.append(ch);
                log.warn("Verbose pos {}: '{}' not found in shuffled alphabet", i, ch);
                continue;
            }

            int offset = encrypt ? 1 : -1;
            int newPos = (idx + offset + alpha.length) % alpha.length;
            char mapped = alpha[newPos];
            char finalCh = lower ? Character.toLowerCase(mapped) : mapped;
            out.append(finalCh);

            dtoDisks.add(new DiskLogDto(i, String.valueOf(ch), lang, new String(alpha), String.valueOf(finalCh)));
            log.debug("Verbose pos {}: '{}' -> '{}' (idx {} -> {})" , i, ch, finalCh, idx, newPos);
        }

        log.debug("transformVerboseInternal completed, output='{}'", out.toString());
        return out.toString();
    }
}
