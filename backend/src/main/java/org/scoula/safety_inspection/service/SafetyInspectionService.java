package org.scoula.safety_inspection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.like.report.service.LikeReportService;
import org.scoula.report.mapper.ReportMapper;
import org.scoula.safety_inspection.infra.bml.service.BuildingManagementLedgerGeneralService;
import org.scoula.safety_inspection.infra.bml.service.BuildingManagementLedgerMultiService;
import org.scoula.safety_inspection.infra.cors.service.CopyOfRegisterGeneralService;
import org.scoula.safety_inspection.infra.cors.service.CopyOfRegisterMultiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.scoula.safety_inspection.infra.analysis.service.AnalysisService;
import org.scoula.report.service.ReportService;
import org.scoula.report.domain.ReportDTO;

import java.util.List;
import java.util.Map;



import lombok.RequiredArgsConstructor;
import org.scoula.like.report.service.LikeReportService;
import org.scoula.report.mapper.ReportMapper;
import org.scoula.safety_inspection.infra.bml.service.BuildingManagementLedgerGeneralService;
import org.scoula.safety_inspection.infra.bml.service.BuildingManagementLedgerMultiService;
import org.scoula.safety_inspection.infra.cors.service.CopyOfRegisterGeneralService;
import org.scoula.safety_inspection.infra.cors.service.CopyOfRegisterMultiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.scoula.safety_inspection.infra.analysis.service.AnalysisService;
import org.scoula.report.service.ReportService;
import org.scoula.report.domain.ReportDTO;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SafetyInspectionService {

    private final AnalysisService analysisService;
    private final CopyOfRegisterMultiService copyOfRegisterMultiService;
    private final CopyOfRegisterGeneralService copyOfRegisterGeneralService;
    private final BuildingManagementLedgerMultiService buildingManagementLedgerMultiService;
    private final BuildingManagementLedgerGeneralService buildingManagementLedgerGeneralService;
    private final ReportService reportService;
    private final ExtractUnicodeService extractUnicodeService;
    private final ReportMapper reportMapper;
    final private LikeReportService likeService;

    /**
     * API 추출 및 DB 저장 트랜잭션 관리를 위한 서비스
     * @param payload
     */
    public String processSafetyInspection(Map<String, Object> payload, String token) {
        try {
            String propertyNo = (String) payload.get("propertyNo");
            Integer analysisNo = analysisService.processPropertyAnalysis(propertyNo, payload);

            String realtyType = (String) payload.get("realtyType");
            System.out.println("realtyType = " + realtyType);

            if (realtyType != null && !realtyType.equals("-1")) {
                if ("1".equals(realtyType)) {
                    System.out.println("resultType : 1");
                    copyOfRegisterMultiService.getCopyOfRegister(payload, analysisNo);
                    buildingManagementLedgerMultiService.getBuildingLedger(payload, analysisNo);
                } else if ("0".equals(realtyType)) {
                    System.out.println("resultType : 0");
                    copyOfRegisterGeneralService.getCopyOfRegister(payload, analysisNo);
                    buildingManagementLedgerGeneralService.getBuildingLedger(payload, analysisNo);
                }
            } else {
            }

            // 보고서 생성 및 저장
            ReportDTO reportDTO = reportService.analysis(analysisNo, propertyNo, payload);
            log.info("AnalysisDate in DTO : " + reportDTO.getAnalysisDate());
            reportService.create(reportDTO, analysisNo, token);
            int reportNo = reportMapper.getReportNo(analysisNo);

            // like report
            likeService.create(reportNo, token);
            return String.valueOf(reportNo);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}