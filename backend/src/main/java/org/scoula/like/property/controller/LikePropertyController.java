package org.scoula.like.property.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.dictionary.domain.DictionaryDTO;
import org.scoula.like.property.domain.LikePropertyDTO;
import org.scoula.like.property.service.LikePropertyService;
import org.scoula.map.domain.MapDetailDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/like/property")
public class LikePropertyController {

    final LikePropertyService service;

    @GetMapping("/{proNo}")
    public ResponseEntity<Integer> create(@RequestHeader("Authorization") String token, @PathVariable int proNo) {
        return ResponseEntity.ok(service.create(proNo, token));
    }

    @GetMapping("")
    public ResponseEntity<List<MapDetailDTO>> getList(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(service.getList(token));
    }

    @DeleteMapping("/{proNo}")
    public ResponseEntity<Integer> delete(@PathVariable int proNo) {
        return ResponseEntity.ok(service.delete(proNo));
    }

}
