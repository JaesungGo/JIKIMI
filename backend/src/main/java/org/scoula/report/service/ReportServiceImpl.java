package org.scoula.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.scoula.oauth.jwt.service.JwtService;
import org.scoula.report.domain.ReportDTO;
import org.scoula.report.domain.ReportVO;
import org.scoula.report.mapper.ReportMapper;
import org.scoula.safety_inspection.infra.bml.dto.BuildingManagementLedgerDto;
import org.scoula.safety_inspection.infra.bml.mapper.BuildingManagementLedgerMapper;
import org.scoula.safety_inspection.infra.cors.dto.CopyOfRegisterDto;
import org.scoula.safety_inspection.infra.cors.mapper.CopyOfRegisterMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    final private ReportMapper mapper;
    final private CopyOfRegisterMapper corMapper;
    final private BuildingManagementLedgerMapper bmlMapper;
    final private JwtService jwtService;

    @Override
    public ReportDTO get(int reportNo, String token) {
        token = token.substring(7);
        String userId = jwtService.getUserIdFromToken(token);

        log.info("reportNo : " + reportNo + "userId : " + userId);
        ReportDTO report = ReportDTO.of(mapper.get(reportNo, userId));
        return Optional.ofNullable(report)
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public int create(ReportDTO report, Integer analysisNo, String token) {
        ReportVO reportVO = report.toVO();
        reportVO.setAnalysisNo(analysisNo);

        return mapper.create(reportVO);
    }

    @Override
    public int delete(int analysisNo) {
        return mapper.delete(analysisNo);
    }

    @Override
    public ReportDTO analysis(int analysisNo, String propertyNo, Map<String, Object> payload) {
        ReportDTO report = new ReportDTO();

        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String type = (value != null) ? value.getClass().getSimpleName() : "null";

            System.out.println(key + " : " + value + " - " + type);
        }

        CopyOfRegisterDto cor = corMapper.selectCopyOfRegister(analysisNo);
        log.info("corDTO :::::::::::::: " + cor);
        BuildingManagementLedgerDto bml = bmlMapper.selectBuildingData(analysisNo);
        log.info("bmlDTO :::::::::::::: " + bml);


        report.setPropertyNo(Integer.parseInt(propertyNo));
        report.setAddress(payload.get("jibunAddress").toString());

        // 날짜
        report.setAnalysisDate(payload.get("analysisDate").toString());
        log.info("DATEDATEDATE : " + report.getAnalysisDate());

        // jeonsePrice: 0, null 예외처리 - jeonseRate을 null 값으로 두고 프론트에서 "판단 불가"로 표기
        if (payload.get("jeonsePrice") != null && payload.get("jeonsePrice") != "") {
            Long jeonsePrice = Long.parseLong(payload.get("jeonsePrice").toString());

            if (jeonsePrice != 0) {
                log.info("jeonsePrice : " + jeonsePrice);
                log.info("Price : " + Long.parseLong(payload.get("price").toString()));
                log.info("jeonseRate : " + 100 * jeonsePrice / Long.parseLong(payload.get("price").toString()));
                report.setJeonseRate((int)(100 * jeonsePrice / Long.parseLong(payload.get("price").toString())));
            }
        } else {
            report.setJeonseRate(null);
        }

        report.setPrice(Integer.parseInt(payload.get("price").toString()));

        // contractName : null, "" 예외처리 - accordOwner을 null 값으로 두고 프론트에서 "판단 불가"로 표기
        // payload.get("contractName")이 List가 아니면 제외

        if(payload.get("contractName") != null && payload.get("contractName") instanceof List<?>) {
            // 공동 소유를 감안하여 리스트에 받음
            List<String> contractNameList = (List<String>) payload.get("contractName");
            log.info("contractNameList : " + contractNameList);
            log.info("Empty : " + contractNameList.isEmpty());
            log.info("contractNameList size : " + contractNameList.size());

            for (int i = 0; i < contractNameList.size(); ++i) {
                if(contractNameList.get(i).trim().isEmpty()) {
                    log.info("비어있는 이름 지움");
                    contractNameList.remove(i);
                }
            }

            if(contractNameList.isEmpty()) {
                log.info("계약자 리스트가 비어있음");
                report.setAccordOwner(null);
            } else {

                String ownership = cor.getOwnership();
                ownership = ownership.replace(" ", "");
                report.setOwnership(ownership);

                // 정규 표현식을 사용하여 이름 부분만 추출
                Pattern pattern = Pattern.compile("([^(]+)\\s*\\(.*?\\)");

                Matcher matcher = pattern.matcher(ownership);

                // 결과를 저장할 List
                List<String> ownershipList = new ArrayList<>();

                // 매칭되는 이름을 찾기
                while (matcher.find()) {
                    if(matcher.group(1).isEmpty()) {
                        log.info("소유자 문자열이 비어있을 때");
                        report.setAccordOwner(null);
                    }
                    ownershipList.add(matcher.group(1)); // 첫 번째 그룹이 이름
                }
                log.info("contractNameList : " + contractNameList);
                report.setAccordOwner(
                        isAccordOwner(contractNameList, ownershipList)
                );
            }
        } else {
            log.info("계약자 값 X");
            report.setAccordOwner(null);
        }

        // 채권최고액이 null이면 판단불가
        if(cor.getMaximumOfBond() == null) {
            report.setMaximumOfBond(null);
        } else {
            report.setMaximumOfBond(cor.getMaximumOfBond());
        }

        // 주용도가 null이면 판단불가
        if(bml.getResContents() == null || bml.getResContents().equals("결과값이 없습니다.")) {
            report.setUseType(null);
        } else {
            report.setUseType(bml.getResContents());
        }

        // 위반건축물이 null이면 판단불가
        if(bml.getResViolationStatus() == null) {
            report.setViolationStructure(null);
        } else {
            report.setViolationStructure(bml.getResViolationStatus());
        }

        // 채권최고액이 null이면 판단불가
        if(cor.getMaximumOfBond() == null) {
            report.setMaximumOfBond(null);
        } else {
            report.setMaximumOfBond(cor.getMaximumOfBond());
        }

        // 주용도가 null이면 판단불가
        if(bml.getResContents() == null) {
            report.setUseType(null);
        } else {
            report.setUseType(bml.getResContents());
        }

        // 위반건축물이 null이면 판단불가
        if(bml.getResViolationStatus() == null) {
            report.setViolationStructure(null);
        } else {
            report.setViolationStructure(bml.getResViolationStatus());
        }

//        report.setKindOfLandrights(cor.getKindOfLandrights());

        // 공동소유/단독소유가 null이면 판단불가
        if(cor.getCommonOwner() == null) {
            report.setCommonOwner(null);
        } else {
            report.setCommonOwner(cor.getCommonOwner());
        }

        // 소유자 변동 횟수가 null이면 판단 불가
        if(cor.getChangeOwnerCount() == null) {
            report.setChangeOwnerCount(null);
        } else {
            report.setChangeOwnerCount(cor.getChangeOwnerCount());
        }

        // 전유부분이 null이면 판단불가
        if(cor.getOwnerState() == null) {
            report.setOwnerState(null);
        } else {
            report.setOwnerState(cor.getOwnerState());
        }

        // 공동소유/단독소유가 null이면 판단불가
        if(cor.getCommonOwner() == null) {
            report.setCommonOwner(null);
        } else {
            report.setCommonOwner(cor.getCommonOwner());
        }

        // 소유자 변동 횟수가 null이면 판단 불가
        if(cor.getChangeOwnerCount() == null) {
            report.setChangeOwnerCount(null);
        } else {
            report.setChangeOwnerCount(cor.getChangeOwnerCount());
        }

        // 전유부분이 null이면 판단불가
        if(cor.getOwnerState() == null) {
            report.setOwnerState(null);
        } else {
            report.setOwnerState(cor.getOwnerState());
        }

        report.setTotalScore(
                getTotalScore(report, Integer.parseInt(payload.get("price").toString()))
        );

        return report;
    }

    public static boolean isAccordOwner(List<String> contractNameList, List<String> ownershipList) {



        // 1. 두 리스트의 크기가 같지 않으면 일대일 매칭 불가
        if (contractNameList.size() != ownershipList.size()) {
            log.info("=============사이즈가 다름=============");
            return false;
        }

        Set<String> conSet = new HashSet<>(contractNameList);
        Set<String> ownSet = new HashSet<>(ownershipList);

        // 2. 각 인덱스의 원소가 같은지 확인
        return conSet.equals(ownSet);
//        for (int i = 0; i < contractNameList.size(); i++) {
//            if (!contractNameList.get(i).equals(ownershipList.get(i))) {
//                return false;
//            }
//        }

        // 모든 원소가 동일하면 true 반환
//        return true;
    }

    private int getTotalScore(ReportDTO report, int price) {

        int totalScore = 100;

        // 전세가율에 따른 감점
        Integer jeonseRate = report.getJeonseRate();
        if(jeonseRate != null) {
            if(jeonseRate > 70) {
                if(jeonseRate <= 80) {
                    log.info("jeonseRate : " + jeonseRate + "로 -7점 감점");
                    totalScore -= 7;
                }
                else if(jeonseRate <= 90) {
                    log.info("jeonseRate : " + jeonseRate + "로 -17점 감점");
                    totalScore -= 17;
                }
                else {
                    log.info("jeonseRate : " + jeonseRate + "로 총점 0점");
                    return 0;
                }
            }
        }

        // 계약자, 소유자 불일치 시 고위험
        if(report.getAccordOwner()!=null && !report.getAccordOwner()) {
            log.info("계약자 - 소유자가 불일치하여 총점 0점 : " + report.getAccordOwner().toString());
            return 0;
        }

        // 근저당권(채권 최고액)에 따른 감점
        Long maximumOfBond = report.getMaximumOfBond();
        if(maximumOfBond != 0) {
            int rate = (int)(maximumOfBond * 100 / price) ;
            if(rate <= 30) {
                log.info("근저당권(채권최고액) : " + rate + "로 -5점 감점");
                totalScore -= 5;
            }
            else if(rate < 50) {
                log.info("근저당권(채권최고액) : " + rate + "로 -10점 감점");
                totalScore -= 10;
            }
            else if(rate < 70) {
                log.info("근저당권(채권최고액) : " + rate + "로 -15점 감점");
                totalScore -= 15;
            }
            else {
                log.info("근저당권(채권최고액) : " + rate + "로 총점 0점");
                return 0;
            }
        }

        // 주용도가 거주가 아니면 고위험 (거주일때 어떻게 값이 들어가는 지 확인 필요)
        String useType = report.getUseType();
        if(useType == null || !(useType.contains("아파트") || useType.contains("주택") || useType.contains("주거") || useType.contains("오피스텔"))) {
            log.info("주용도가 거주용이 아니라 총점 0점");
            return 0;
        }

        // 위반 건축물이면 고위험
        if(report.getViolationStructure() == null || report.getViolationStructure()) {
            log.info("위반 건축물이라 총점 0점");
            return 0;
        }

//        // 대지권 등기에 따른 감점 (대지권 등기에 따라 어떻게 값이 들어가는 지 확인 필요)
//        String landrights = report.getKindOfLandrights();
//        if(landrights.equals("미등기"))
//            totalScore -= 10;
//        else if(landrights.equals("불명확"))
//            totalScore -= 15;
//        else if(landrights.equals("없음"))
//            return 0;

        // 총점 50점 기준
        if(totalScore < 70) {
            log.info("총점이 70점 미만이라 총점 0점");
            return 0;
        }

        return totalScore;
    }
}