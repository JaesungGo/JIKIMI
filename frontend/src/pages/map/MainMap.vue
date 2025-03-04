<script setup>
import { ref, onMounted, computed } from 'vue';
import addressApi from '@/api/mapApi';
import LeftPanel from './LeftPanel.vue';

const mapContainer = ref(null);
const coordinates = ref([]);
let map, clusterer, marker;
let markers = [];
let overlays = [];
let isFetching = false;
let cancelPagination = false;

// 데이터 캐싱을 위한 객체 추가
const dataCache = {};
const CACHE_MAX_SIZE = 5; // 캐시할 최대 지역 수

const selectedProperty = ref(null);
const isPanelOpen = ref(false);
const isDetailsVisible = ref(false);

const toggleButtonText = computed(() => {
  return isPanelOpen.value ? '지도 확대' : '지도 축소';
});

// 캐시 크기 제한 함수
const limitCacheSize = (cache, maxSize) => {
  const keys = Object.keys(cache);
  if (keys.length > maxSize) {
    // 가장 오래된 캐시 항목 삭제
    delete cache[keys[0]];
  }
};

// 리소스 정리 함수 - 메모리 관리를 위해 명시적으로 마커와 오버레이 제거
const clearResources = () => {
  // 기존 마커 모두 제거
  markers.forEach(marker => marker.setMap(null));
  
  // 기존 오버레이 모두 제거
  overlays.forEach(overlay => overlay.setMap(null));
  
  // 배열 초기화
  markers = [];
  overlays = [];
};

// 데이터 로드 함수 개선
const fetchAddressData = async (lat, lon, zoomLevel, page = 1, limit = 200) => {
  if (cancelPagination) {
    console.log('Pagination cancelled.');
    return;
  }

  // 캐시 키 생성
  const cacheKey = `${lat.toFixed(3)}_${lon.toFixed(3)}_${zoomLevel}`;

  // 낮은 줌 레벨일 때 처리 (상세 지도 표시)
  if (zoomLevel < 5) {
    try {
      isFetching = true;
      
      // 캐시에 데이터가 있는지 확인
      if (dataCache[cacheKey]) {
        console.log('Using cached data');
        const newCoordinates = dataCache[cacheKey];
        
        // 클러스터러 초기화
        clusterer.clear();
        
        // 기존 좌표 배열 대체 (누적 대신)
        coordinates.value = newCoordinates.slice();
        
        // 마커 업데이트
        updateMarkers(newCoordinates);
      } else {
        // API 호출
        const response = await addressApi.getAddressListMoveAll(lat, lon, zoomLevel);
        const newCoordinates = response.map(item => ({
          id: item.locationNo,
          x: parseFloat(item.xcoordinate),
          y: parseFloat(item.ycoordinate),
          price: item.price,
        }));
        
        // 캐시에 저장
        dataCache[cacheKey] = newCoordinates;
        limitCacheSize(dataCache, CACHE_MAX_SIZE);
        
        // 클러스터러 초기화
        clusterer.clear();
        
        // 기존 좌표 배열 대체 (누적 대신)
        coordinates.value = newCoordinates.slice();
        
        // 마커 업데이트
        updateMarkers(newCoordinates);
      }
    } catch (error) {
      console.error('Failed to fetch address data:', error);
    } finally {
      isFetching = false;
    }
  } else {
    // 높은 줌 레벨에서 클러스터 처리 (개선된 페이징)
    try {
      isFetching = true;
      
      // 재귀 호출 대신 반복문으로 구현
      let allCoordinates = [];
      let currentPage = page;
      let hasMoreData = true;
      
      while (hasMoreData && !cancelPagination) {
        const response = await addressApi.getAddressListMoveClusterAll(
          lat, lon, zoomLevel, currentPage, limit
        );
        
        const newCoordinates = response.map(item => ({
          id: item.locationNo,
          x: parseFloat(item.xcoordinate),
          y: parseFloat(item.ycoordinate),
          price: item.price,
        }));
        
        allCoordinates = [...allCoordinates, ...newCoordinates];
        
        // 더 이상 데이터가 없거나 최대 페이지에 도달하면 종료
        if (newCoordinates.length < limit || currentPage >= 5) {
          hasMoreData = false;
        } else {
          currentPage++;
        }
      }
      
      // 캐시에 저장
      dataCache[cacheKey] = allCoordinates;
      limitCacheSize(dataCache, CACHE_MAX_SIZE);
      
      // 기존 모든 오버레이 제거
      overlays.forEach(overlay => overlay.setMap(null));
      overlays = [];
      
      // 클러스터 마커 업데이트 
      if (allCoordinates.length > 0) {
        updateMarkersCluster(allCoordinates);
      }
    } catch (error) {
      console.error('Failed to fetch address data:', error);
    } finally {
      isFetching = false;
    }
  }
};

// 지도 및 마커 클러스터러 초기화
const initializeMap = () => {
  let mapContainer = document.getElementById('map'),
    mapOption = {
      center: new kakao.maps.LatLng(37.5538265102548, 126.968466927468),
      level: 2,
    };

  map = new kakao.maps.Map(mapContainer, mapOption);
  map.setMaxLevel(12);
  
  // 마커 클러스터러 초기화
  clusterer = new kakao.maps.MarkerClusterer({
    map: map,
    averageCenter: true,
    minLevel: 5,
    minClusterSize: 2,
    disableClickZoom: false,
    averageCenter: false,
  });

  // 클러스터 크기에 따른 문구 표시
  clusterer.setTexts(function (size) {
    if (size >= 1000) return '1000+';
    else if (size >= 500) return '500+';
    else if (size >= 200) return '200+';
    else if (size >= 100) return '100+';
    else if (size >= 50) return '50+';
    else return size.toString();
  });

  // 클러스터 크기를 구분하는 기준 설정
  clusterer.setCalculator(function (size) {
    if (size < 50) return 0;
    else if (size < 100) return 1;
    else if (size < 200) return 2;
    else if (size < 500) return 3;
    else if (size < 1000) return 4;
    else return 5;
  });
  
  // 지도 이동 이벤트 리스너 최적화
  let idleTimer = null;
  kakao.maps.event.addListener(map, 'idle', function () {
    // 진행 중인 요청 취소
    if (isFetching) {
      cancelPagination = true;
    }
    
    // 디바운싱 적용 - 지도 이동이 연속으로 발생할 때 마지막 이벤트만 처리
    clearTimeout(idleTimer);
    idleTimer = setTimeout(() => {
      if (map) {
        cancelPagination = false;
        
        // 이전 리소스 정리
        clearResources();
        
        // 클러스터에서 마커 제거
        clusterer.clear();
        
        const level = map.getLevel();
        const center = map.getCenter();
        const lat = center.getLat();
        const lon = center.getLng();
        
        // 새 데이터 요청
        fetchAddressData(lat, lon, level);
      }
    }, 300); // 디바운스 타임
  });

  // 컨트롤 추가
  map.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);
  map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

  // 초기 위치 설정
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const currentLat = position.coords.latitude;
        const currentLon = position.coords.longitude;
        const currentPosition = new kakao.maps.LatLng(currentLat, currentLon);
        map.setCenter(currentPosition);
        map.setLevel(5);
      },
      (error) => {
        console.error('Geolocation failed: ', error);
      }
    );
  }
};

// 즐겨찾기 선택시 이동하는 함수
const moveToLikedProperty = async (wishlist) => {
  try {
    const data = await addressApi.getAddressDetails(wishlist.locationNo);
    selectedProperty.value = data;
    
    if (data && data[0].xcoordinate && data[0].ycoordinate) {
      setMapCoordinates({
        x: data[0].xcoordinate,
        y: data[0].ycoordinate,
        buildingName: data[0].propertyAddrAptName,
        doroJuso: data[0].doroJuso,
      });
    } else {
      console.error('Invalid coordinates:', data);
    }
  } catch (error) {
    console.error('Failed to fetch address details:', error);
  }
};

// 검색을 통해 지도를 특정 좌표로 이동시키는 함수
const setMapCoordinates = ({ x, y, buildingName, doroJuso }) => {
  setTimeout(() => {
    if (map) {
      const coords = new kakao.maps.LatLng(y, x);
      map.panTo(coords);
      map.setCenter(coords);
      
      if (marker) {
        marker.setPosition(coords);
      } else {
        marker = new kakao.maps.Marker({
          position: coords,
          map: map,
          image: new kakao.maps.MarkerImage(
            '../../src/assets/marker.svg',
            new kakao.maps.Size(40, 40)
          ),
        });
      }
      
      if (buildingName.startsWith('(')) {
        buildingName = doroJuso;
      }
      
      map.setLevel(2, { animate: true });
    } else {
      console.error('Map is not initialized yet.');
    }
  }, 50);
};

// 마커 이미지 설정 - 메모리 절약을 위해 재사용
const markerImage = new kakao.maps.MarkerImage(
  '../../src/assets/image-2.svg',
  new kakao.maps.Size(75, 75)
);
const selectedMarkerImage = new kakao.maps.MarkerImage(
  '../../src/assets/image-2.svg',
  new kakao.maps.Size(105, 105)
);
const clickedMarkerImage = new kakao.maps.MarkerImage(
  '../../src/assets/image (3).png',
  new kakao.maps.Size(75, 75)
);

let clickedMarker = null;

// 마커 업데이트 함수 개선
const updateMarkers = (newCoords) => {
  // 기존 마커와 오버레이 제거
  clearResources();
  
  // 메모리를 위해 한 번에 처리할 마커 제한
  const maxMarkers = 300;
  const coordsToProcess = newCoords.slice(0, maxMarkers);
  
  const newMarkers = coordsToProcess.map((coord) => {
    const markerPosition = new kakao.maps.LatLng(coord.y, coord.x);
    const marker = new kakao.maps.Marker({
      position: markerPosition,
      image: markerImage,
      zIndex: 2,
    });

    // 커스텀 오버레이 간소화
    const overlayContent = document.createElement('div');
    overlayContent.className = 'customoverlay';
    overlayContent.innerHTML = `<div class="pricePopup"
                                     style="position:relative;
                                            top: 5px;
                                            font-size: 11px;
                                            font-weight: bold;
                                            right: 5.5px;"
                                    >${coord.price}억
                                </div>`;

    const customOverlay = new kakao.maps.CustomOverlay({
      position: markerPosition,
      content: overlayContent,
      clickable: false,
      yAnchor: 3,
      zIndex: 3,
    });

    overlays.push(customOverlay);
    customOverlay.setMap(map);

    // 이벤트 핸들러 간소화
    const applyHoverStyle = (isHover) => {
      if (marker !== clickedMarker) {
        marker.setImage(isHover ? selectedMarkerImage : markerImage);
      }
      const pricePopup = overlayContent.querySelector('.pricePopup');
      if (pricePopup) {
        pricePopup.style.fontSize = isHover ? '15px' : '11px';
        pricePopup.style.top = isHover ? '-14px' : '5px';
        pricePopup.style.right = isHover ? '14.5px' : '5.5px';
      }
    };

    // 효율적인 이벤트 핸들링
    const eventHandlers = {
      mouseover: () => {
        if (clickedMarker !== marker) applyHoverStyle(true);
      },
      mouseout: () => applyHoverStyle(false),
      click: async () => {
        if (clickedMarker === marker) {
          marker.setImage(markerImage);
          clickedMarker = null;
        } else {
          if (clickedMarker) {
            clickedMarker.setImage(markerImage);
          }
          marker.setImage(clickedMarkerImage);
          clickedMarker = marker;
        }
        
        try {
          const data = await addressApi.getAddressDetails(coord.id);
          selectedProperty.value = data;
          isDetailsVisible.value = true;
          
          if (!isPanelOpen.value) {
            togglePanel();
          }
        } catch (error) {
          console.error('Failed to fetch address details:', error);
        }
      }
    };

    // 이벤트 등록
    kakao.maps.event.addListener(marker, 'mouseover', eventHandlers.mouseover);
    kakao.maps.event.addListener(marker, 'mouseout', eventHandlers.mouseout);
    kakao.maps.event.addListener(marker, 'click', eventHandlers.click);

    overlayContent.addEventListener('mouseover', eventHandlers.mouseover);
    overlayContent.addEventListener('mouseout', eventHandlers.mouseout);
    overlayContent.addEventListener('click', (e) => {
      e.stopPropagation();
      eventHandlers.click();
    });

    return marker;
  });

  // 마커 배열 및 클러스터에 추가
  markers = newMarkers;
  clusterer.addMarkers(newMarkers);

  // 클러스터 이벤트: 클러스터 형성 시 오버레이 숨기기
  kakao.maps.event.addListener(clusterer, 'clustered', () => {
    overlays.forEach(overlay => overlay.setMap(null));
  });
};

// 클러스터 마커 업데이트 함수 개선
const updateMarkersCluster = (newCoords) => {
  // 기존 마커 제거
  markers.forEach(marker => marker.setMap(null));
  markers = [];

  // 작은 투명 마커 이미지 (메모리 최적화)
  const markerImageSVG = new kakao.maps.MarkerImage(
    '../../src/assets/marker.svg',
    new kakao.maps.Size(0, 0)
  );

  // 메모리를 위해 한 번에 처리할 마커 제한
  const maxMarkers = 1000;
  const coordsToProcess = newCoords.slice(0, maxMarkers);

  // 배치 처리 최적화
  const batchSize = 200;
  let currentIndex = 0;

  // GC를 위해 함수 외부의 변수 사용 제한
  const processNextBatch = () => {
    if (currentIndex >= coordsToProcess.length || cancelPagination) {
      return;
    }
    
    const endIndex = Math.min(currentIndex + batchSize, coordsToProcess.length);
    const batchMarkers = [];
    
    for (let i = currentIndex; i < endIndex; i++) {
      const coord = coordsToProcess[i];
      const markerPosition = new kakao.maps.LatLng(coord.y, coord.x);
      const marker = new kakao.maps.Marker({
        position: markerPosition,
        image: markerImageSVG
      });
      
      batchMarkers.push(marker);
      markers.push(marker);
    }
    
    clusterer.addMarkers(batchMarkers);
    currentIndex = endIndex;
    
    // 비동기로 다음 배치 처리 (call stack 최적화)
    if (currentIndex < coordsToProcess.length && !cancelPagination) {
      setTimeout(processNextBatch, 0);
    }
  };
  
  processNextBatch();
};

const togglePanel = () => {
  isPanelOpen.value = !isPanelOpen.value;
  
  // 패널 토글 후 지도 리사이즈 - 성능 최적화를 위해 지연
  setTimeout(() => {
    if (map) {
      map.relayout();
    }
  }, 100);
};

// 지도 컨트롤러 관련 기능
let mapTypeControl = new kakao.maps.MapTypeControl();
let zoomControl = new kakao.maps.ZoomControl();

let mapTypes = {
  useDistrict: kakao.maps.MapTypeId.USE_DISTRICT,
  terrain: kakao.maps.MapTypeId.TERRAIN,
  traffic: kakao.maps.MapTypeId.TRAFFIC,
};

// 전역 함수 메모리 최적화
window.setOverlayMapTypeId = function () {
  const chkUseDistrict = document.getElementById('chkUseDistrict');
  const chkTerrain = document.getElementById('chkTerrain');
  const chkTraffic = document.getElementById('chkTraffic');

  // 지도 타입 초기화
  for (const type in mapTypes) {
    map.removeOverlayMapTypeId(mapTypes[type]);
  }

  // 선택된 옵션만 추가
  if (chkUseDistrict && chkUseDistrict.checked) {
    map.addOverlayMapTypeId(mapTypes.useDistrict);
  }
  if (chkTerrain && chkTerrain.checked) {
    map.addOverlayMapTypeId(mapTypes.terrain);
  }
  if (chkTraffic && chkTraffic.checked) {
    map.addOverlayMapTypeId(mapTypes.traffic);
  }
};

// 현재 위치로 이동 함수 (메모리 최적화)
window.moveToCurrentLocation = function () {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const currentPosition = new kakao.maps.LatLng(
          position.coords.latitude,
          position.coords.longitude
        );
        map.setCenter(currentPosition);
        map.setLevel(5);
      },
      (error) => {
        console.error('Geolocation failed: ', error);
        alert('현재 위치를 가져올 수 없습니다.');
      }
    );
  } else {
    alert('현재 위치 기능을 사용할 수 없습니다.');
  }
};

// 컴포넌트 마운트 시 지도 초기화
onMounted(() => {
  // 지도 초기화 시간 지연
  setTimeout(() => {
    initializeMap();
  }, 0);
});
</script>

<template>
  <div class="container">
    <LeftPanel
      v-if="isPanelOpen"
      :selectedProperty="selectedProperty"
      :is-details-visible="isDetailsVisible"
      @toggle-panel="togglePanel"
      @move-map-to-coordinates="setMapCoordinates"
      @favoriteItem="moveToLikedProperty"
      @update:isDetailsVisible="(val) => (isDetailsVisible = val)"
    />

    <div
      :class="{
        'right-panel-full': !isPanelOpen,
        'right-panel': isPanelOpen,
      }"
    >
      <button class="toggle-panel-btn" @click="togglePanel">
        {{ toggleButtonText }}
      </button>

      <div id="map" ref="mapContainer"></div>
      
      <div class="custom_maptypecontrol">
        <input
          type="checkbox"
          id="chkUseDistrict"
          onclick="setOverlayMapTypeId()"
        />지적편집도
        <input
          type="checkbox"
          id="chkTerrain"
          onclick="setOverlayMapTypeId()"
        />지형정보
        <input
          type="checkbox"
          id="chkTraffic"
          onclick="setOverlayMapTypeId()"
        />교통정보
      </div>
      
      <button
        class="location-btn"
        onclick="moveToCurrentLocation()"
      >
        <i class="fa-solid fa-location-crosshairs"></i>
      </button>
    </div>
  </div>
</template>

<style scoped>
body {
  margin: 0;
  padding: 0;
}

.container {
  width: 100%;
  display: flex;
  height: 80vh;
  max-width: 100vw;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.container div {
  border-bottom: none;
}

.left-panel {
  width: 25%;
  padding: 20px;
  background-color: white;
  /* position: relative; */
  border: 1px black;
  height: 100%;
  box-sizing: border-box;
}

.right-panel {
  width: 75%;
  position: relative;
  height: 90vh;
}

.right-panel-full {
  width: 100%;
  position: relative;
}

/* 수정 1: 지도 좌측 상단에 추가된 버튼 스타일 */
.toggle-panel-btn {
  position: absolute;
  top: 15px;
  left: 10px;
  z-index: 1000;
  background-color: #007bff;
  color: white;
  border: none;
  padding: 10px;
  cursor: pointer;
  border-radius: 5px;
}

.toggle-panel-btn:hover {
  background-color: #0056b3;
}

#map {
  width: 100%;
  height: 100%;
}

/* 버튼을 헤더가 아닌 지도 위에 정확히 위치시킴 */
#map .toggle-panel-btn {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 100;
}

/* 지도 아래쪽에 지적편집도, 지형정보, 교통정보 버튼 컨트롤 */
.right-panel-full .custom_maptypecontrol {
  position: absolute;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  bottom: 3%;
  left: 44%;
  width: auto;
  z-index: 1000;
  justify-content: space-between;
  font-family: 'Malgun Gothic', '맑은 고딕', sans-serif;
  font-size: 18px;
  cursor: pointer;
  opacity: 0.5;
}

.right-panel .custom_maptypecontrol {
  position: absolute;
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  bottom: 15%;
  left: 35%;
  width: auto;
  z-index: 1000;
  justify-content: space-between;
  font-family: 'Malgun Gothic', '맑은 고딕', sans-serif;
  font-size: 18px;
  cursor: pointer;
  opacity: 0.5;
}

.custom_maptypecontrol:hover {
  border-color: grey;
  opacity: 1;
}
.custom_maptypecontrol input {
  margin-left: 15px;
  border-radius: 5px;
  /* background-color: #fff; */
}

.right-panel .location-btn {
  position: absolute;
  bottom: 15%;
  right: 8%;
  z-index: 1000;
  background-color: #007bff;
  opacity: 0.3;
  color: white;
  border: none;
  padding: 15px;
  cursor: pointer;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.right-panel-full .location-btn {
  position: absolute;
  bottom: 4.5%;
  right: 6%;
  z-index: 1000;
  background-color: #007bff;
  opacity: 0.3;
  color: white;
  border: none;
  padding: 15px;
  cursor: pointer;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.location-btn:hover {
  background-color: #0056b3;
  opacity: 1;
}

.font-awesome-icon {
  font-size: 20px;
}
</style>
