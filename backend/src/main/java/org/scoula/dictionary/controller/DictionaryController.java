package org.scoula.dictionary.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.dictionary.domain.DictionaryDTO;
import org.scoula.dictionary.service.DictionaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/dictionary")
public class DictionaryController {

    final DictionaryService service;

    @GetMapping("")
    public ResponseEntity<List<DictionaryDTO>> getList() {
        return ResponseEntity.ok(service.getList());
    }

    @GetMapping("/{no}")
    public ResponseEntity<DictionaryDTO> get(@PathVariable int no) {
        return ResponseEntity.ok(service.get(no));
    }

}
