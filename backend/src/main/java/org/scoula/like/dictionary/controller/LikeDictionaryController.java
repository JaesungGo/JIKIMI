package org.scoula.like.dictionary.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.dictionary.domain.DictionaryDTO;
import org.scoula.like.dictionary.service.LikeDictionaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/like/dictionary")
public class LikeDictionaryController {

    final LikeDictionaryService service;

    @GetMapping("/{dicNo}")
    public ResponseEntity<Integer> create( @RequestHeader("Authorization") String token, @PathVariable int dicNo) {
        return ResponseEntity.ok(service.create(dicNo, token));
    }

    @GetMapping("")
    public ResponseEntity<List<DictionaryDTO>> getList(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(service.getList(token));
    }

    @DeleteMapping("/{dicNo}")
    public ResponseEntity<Integer> delete(@PathVariable int dicNo) {
        return ResponseEntity.ok(service.delete(dicNo));
    }

}
