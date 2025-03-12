package org.scoula.like.report.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.common.pagination.Page;
import org.scoula.common.pagination.PageRequest;
import org.scoula.like.report.service.LikeReportService;
import org.scoula.report.domain.ReportDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/like/report")
public class LikeReportController {

    final LikeReportService service;

    @GetMapping("/{reportNo}")
    public ResponseEntity<Integer> create(@RequestHeader("Authorization") String token, @PathVariable int reportNo) {
        return ResponseEntity.ok(service.create(reportNo, token));
    }

//    @GetMapping("")
//    public ResponseEntity<List<ReportDTO>> getList(@RequestHeader("Authorization") String token) {
//        return ResponseEntity.ok(service.getList(token));
//    }

    @GetMapping("")
    public ResponseEntity<Page> getList(@RequestHeader("Authorization") String token, PageRequest pageRequest) {
        return ResponseEntity.ok(service.getPage(token, pageRequest));
    }

    @DeleteMapping("/{memberReportNo}")
    public ResponseEntity<Integer> delete(@PathVariable int memberReportNo) {
        return ResponseEntity.ok(service.delete(memberReportNo));
    }
}
