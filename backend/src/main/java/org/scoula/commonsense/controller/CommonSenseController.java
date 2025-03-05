package org.scoula.commonsense.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.pagination.Page;
import org.scoula.common.pagination.PageRequest;
import org.scoula.commonsense.domain.CommonSenseDTO;
import org.scoula.commonsense.service.CommonSenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/common-sense")
public class CommonSenseController {

    final CommonSenseService service;

    @GetMapping("")
    public ResponseEntity<Page> getList(PageRequest pageRequest) {
        return ResponseEntity.ok(service.getPage(pageRequest));
    }

    @GetMapping("/{no}")
    public ResponseEntity<CommonSenseDTO> get(@PathVariable int no) {
        return ResponseEntity.ok(service.get(no));
    }
}
