package org.scoula.safety_inspection.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.report.domain.ReportDTO;
import org.scoula.report.service.ReportService;
import org.scoula.safety_inspection.infra.analysis.service.AnalysisService;
import org.scoula.safety_inspection.infra.bml.service.BuildingManagementLedgerGeneralService;
import org.scoula.safety_inspection.infra.bml.service.BuildingManagementLedgerMultiService;
import org.scoula.safety_inspection.infra.cors.service.CopyOfRegisterGeneralService;
import org.scoula.safety_inspection.infra.cors.service.CopyOfRegisterMultiService;
import org.scoula.safety_inspection.service.ExtractUnicodeService;
import org.scoula.safety_inspection.service.SafetyInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/safety-inspection")
@RequiredArgsConstructor
public class SafetyInspectionController {
    final private ExtractUnicodeService extractUnicodeService;
    final private SafetyInspectionService safetyInspectionService;

    // 유니크 코드 관련
    @PostMapping("/address")
    public ResponseEntity<List<Map<String, String>>> handleAccess(@RequestBody Map<String, Object> payload) {
        try {
            for (Map.Entry<String, Object> entry : payload.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String type = (value != null) ? value.getClass().getSimpleName() : "null";

                System.out.println(key + " : " + value + " - " + type);
            }

//            if (payload != null) {
//                payload.forEach((key, value) ->
//                        System.out.printf("Key: %s, Value: %s, Type: %s%n",
//                                key, value, value == null ? "null" : value.getClass().getSimpleName()));
//            }
//            return null;

            List<Map<String,String>> response = extractUnicodeService.getUniqueCode(payload);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(List.of(Map.of("error", "An error occurred while processing the request.")), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // 등기부 등본 관련
    @PostMapping("/cors")
    public ResponseEntity<String> handleUniqueCode(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> payload) {
        try {

            String reportNo = safetyInspectionService.processSafetyInspection(payload, token);
            return ResponseEntity.ok(reportNo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("실패: " + e.getMessage());
        }
    }
}