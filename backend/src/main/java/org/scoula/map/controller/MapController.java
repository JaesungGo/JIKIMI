package org.scoula.map.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoula.map.domain.MapClusterVO;
import org.scoula.map.domain.MapDetailDTO;
import org.scoula.map.domain.MapVO;
import org.scoula.map.service.MapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
@Slf4j
public class MapController {

    private final MapService addressService;

    @GetMapping("")
    public ResponseEntity<List<MapVO>> getAddressList() {
        List<MapVO> addresses = addressService.getAllAddress();
        return ResponseEntity.ok(addresses);
    }

    // 중심 좌표와 레벨을 기반으로 해당 구역 안에 있는 주소 리스트를 반환하는 API
    @GetMapping("/moveall")
    public ResponseEntity<List<MapVO>> getAddressListMoveAll(@RequestParam("lat") double lat, @RequestParam("lon") double lon, @RequestParam("zoomLevel") int zoomLevel) {
        List<MapVO> addressList = addressService.getAddressListMoveAll(lat, lon, zoomLevel);
        return ResponseEntity.ok(addressList);
    }


    @GetMapping("/moveclusterall")
    public ResponseEntity<List<MapClusterVO>> getAddressListMoveClusterAll(@RequestParam("lat") double lat, @RequestParam("lon") double lon, @RequestParam("zoomLevel") int zoomLevel, @RequestParam("page") int page,  // 페이지 번호 추가
                                                                           @RequestParam("limit") int limit // 한 번에 가져올 데이터 개수 추가
    ) {
        List<MapClusterVO> addressList = addressService.getAddressListMoveClusterAll(lat, lon, zoomLevel, page, limit);
        return ResponseEntity.ok(addressList);
    }


    @GetMapping("/{id}")
    public ResponseEntity<List<MapDetailDTO>> getAddressDetails(@PathVariable Long id) {
        List<MapDetailDTO> addressDetails = addressService.getAddressDetails(id); // List로 변경
        return ResponseEntity.ok(addressDetails);
    }
    @GetMapping("/report/{id}")
    public ResponseEntity<List<MapDetailDTO>> getReportAddressDetails(@PathVariable Long id) {
        List<MapDetailDTO> addressDetails = addressService.getReportAddressDetails(id); // List로 변경
        return ResponseEntity.ok(addressDetails);
    }
}