package org.scoula.test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.report.domain.ReportDTO;
import org.scoula.report.service.ReportService;
import org.scoula.test.common.BmlInfo;
import org.scoula.test.common.CorInfo;
import org.scoula.test.common.Payload;
import org.scoula.test.service.TestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final TestService service;

    private final ReportService reportService;

    @GetMapping("/create/cor")
    public ResponseEntity<Integer> createCor(CorInfo corInfo) {
        log.info("CreateCor start ---------------------------------------");

        return ResponseEntity.ok(service.createCor(corInfo));
    }

    @GetMapping("/create/bml")
    public ResponseEntity<Integer> createBml(BmlInfo bmlInfo) {
        log.info("CreateBml start ---------------------------------------");
        return ResponseEntity.ok(service.createBml(bmlInfo));
    }

    @GetMapping("/analysis")
    public ResponseEntity<ReportDTO> analysis(Payload payload) {

        log.info("-----------------------------");
        log.info(payload.toString());
        // 파싱
        int analysisNo = payload.getAnalysisNo();
        String propertyNo = payload.getPropertyNo();
        log.info(propertyNo);

        Map<String, Object> payloadMap = new HashMap<>();

        payloadMap.put("analysisNo", payload.getAnalysisNo());
        payloadMap.put("addrSido", payload.getAddrSido());
        payloadMap.put("addrDong", payload.getAddrDong());
        payloadMap.put("addrLotNumber", payload.getAddrLotNumber());
        payloadMap.put("buildingName", payload.getBuildingName());
        payloadMap.put("dong", payload.getDong());
        payloadMap.put("ho", payload.getHo());
        payloadMap.put("zipCode", payload.getZipCode());
        payloadMap.put("jeonsePrice", payload.getJeonsePrice());
        payloadMap.put("contractName", payload.getContractName());
        payloadMap.put("jibunAddress", payload.getJibunAddress());
        payloadMap.put("propertyNo", payload.getPropertyNo());
        payloadMap.put("price", payload.getPrice());
        payloadMap.put("analysisDate", payload.getAnalysisDate());


        return ResponseEntity.ok(reportService.analysis(analysisNo, propertyNo, payloadMap));
    }
}
