<template>
  <div class="left-panel">
    <div class="search">
      <SearchBar
        @address-selected="handleAddressSelected"
      />
    </div>

    <div class="panel-section wishlist-toggle">
      <div class="section-header" @click="toggleWishlist">
        <span class="header-text">즐겨찾기</span>
        <span
          class="hamburger-menu"
          :class="{ active: wishlistVisible }"
        >
          <div class="bar"></div>
          <div class="bar"></div>
          <div class="bar"></div>
        </span>
      </div>
      <div v-if="wishlistVisible" class="section-content">
        <ul v-if="wishlist.length" class="wishlist">
          <li
            v-for="wish in wishlist"
            :key="wish.propertyNo"
            @click="
              selectApartment(wish.propertyAddrAptName)
            "
            class="wishlist-item"
          >
            <span @click="favoriteClick(wish)">{{
              wish.doroJuso
            }}</span>
            <font-awesome-icon
              class="favorite-icon"
              :icon="['fas', 'star']"
              @click.stop="
                removeFromWishlist(wish.propertyNo)
              "
              style="color: #ffd43b"
            />
          </li>
        </ul>
        <p v-else class="empty-message">
          즐겨찾기가 비어있습니다.
        </p>
      </div>
    </div>
    <div class="panel-section detail-toggle">
      <div class="section-header" @click="toggleDetails">
        <span class="header-text">상세보기</span>
        <span
          class="hamburger-menu"
          :class="{ active: isDetailsVisible }"
        >
          <div class="bar"></div>
          <div class="bar"></div>
          <div class="bar"></div>
        </span>
      </div>
      <div v-if="isDetailsVisible" class="section-content">
        <div
          v-if="selectedProperty && selectedProperty.length"
          class="property-details"
        >
          <div class="property-header">
            <h2 class="apart-name">
              {{ selectedProperty[0].propertyAddrAptName }}
              <font-awesome-icon
                class="favorite-icon"
                :icon="
                  isFavorite
                    ? ['fas', 'star']
                    : ['far', 'star']
                "
                :style="{
                  color: isFavorite ? '#FFD43B' : '#8E8E8E',
                }"
                @click="toggleFavorite"
              />
            </h2>
            <h4 class="address">
              {{ selectedProperty[0].doroJuso }}
              <span class="year"
                >(건축년도:
                {{
                  selectedProperty[0].buildingYear
                }})</span
              >
            </h4>
          </div>

          <div class="table-container">
            <table class="property-table">
              <thead>
                <tr>
                  <th>계약일자</th>
                  <th>가격</th>
                  <th>면적</th>
                  <th>층</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(
                    property, index
                  ) in selectedProperty"
                  :key="index"
                >
                  <td>{{ property.date }}</td>
                  <td>{{ property.price }}억 원</td>
                  <td>{{ property.propertyArea }} m²</td>
                  <td>{{ property.propertyAddrFloor }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="analyze-button-container">
            <button
              class="analyze-button"
              @click="analyzeProperty"
            >
              매물 분석하기
            </button>
          </div>
        </div>
        <p v-else class="empty-message">
          매물을 골라주세요.
        </p>
      </div>
    </div>

    <div class="panel-section image-search-toggle">
      <div
        class="section-header"
        @click="toggleImageSearch"
      >
        <span class="header-text">관련 이미지</span>
        <span
          class="hamburger-menu"
          :class="{ active: imageSearchVisible }"
        >
          <div class="bar"></div>
          <div class="bar"></div>
          <div class="bar"></div>
        </span>
      </div>
      <div
        v-if="imageSearchVisible"
        class="section-content"
      >
        <div v-if="isImageLoading" class="loading-spinner">
          <div class="spinner"></div>
        </div>
        <div
          v-else-if="imageResults.length"
          class="image-results"
        >
          <div
            v-for="image in imageResults"
            :key="image.link"
            class="image-result"
          >
            <img
              :src="image.thumbnail"
              :alt="image.title"
              @error="handleImageError"
              @click="openImageModal(image)"
            />
          </div>
        </div>
        <p v-else class="empty-message">
          관련 이미지가 없습니다.
        </p>
      </div>
    </div>

    <div class="panel-section blog-search-toggle">
      <div class="section-header" @click="toggleBlogSearch">
        <span class="header-text">관련 블로그</span>
        <span
          class="hamburger-menu"
          :class="{ active: blogSearchVisible }"
        >
          <div class="bar"></div>
          <div class="bar"></div>
          <div class="bar"></div>
        </span>
      </div>
      <div v-if="blogSearchVisible" class="section-content">
        <div v-if="isLoading" class="loading-spinner">
          <div class="spinner"></div>
        </div>
        <div
          v-else-if="blogPosts.length"
          class="blog-posts"
        >
          <div
            v-for="post in blogPosts"
            :key="post.link"
            class="blog-post"
          >
            <a
              :href="post.link"
              target="_blank"
              rel="noopener noreferrer"
              class="blog-post-link"
            >
              <h3
                class="blog-post-title"
                v-html="post.title"
              ></h3>
              <p
                class="blog-post-description"
                v-html="post.description"
              ></p>
              <div class="blog-post-info">
                <span class="blog-post-date">{{
                  formatDate(post.postdate)
                }}</span>
                <span class="blog-post-blogger">{{
                  post.bloggername
                }}</span>
              </div>
            </a>
          </div>
        </div>
        <p v-else class="empty-message">
          관련 블로그 포스트가 없습니다.
        </p>
      </div>
    </div>
    <div class="panel-section local-search-toggle">
      <div
        class="section-header"
        @click="toggleLocalSearch"
      >
        <span class="header-text">주변 정보</span>
        <span
          class="hamburger-menu"
          :class="{ active: localSearchVisible }"
        >
          <div class="bar"></div>
          <div class="bar"></div>
          <div class="bar"></div>
        </span>
      </div>
      <div
        v-if="localSearchVisible"
        class="section-content"
      >
        <div v-if="isLocalLoading" class="loading-spinner">
          <div class="spinner"></div>
        </div>
        <div
          v-else-if="localResults.length"
          class="local-results"
        >
          <div
            v-for="result in localResults"
            :key="result.link"
            class="local-result"
          >
            <h3
              class="local-result-title"
              v-html="result.title"
            ></h3>
            <p class="local-result-category">
              {{ result.category }}
            </p>
            <p
              class="local-result-address"
              v-html="result.address"
            ></p>
            <p
              class="local-result-description"
              v-html="result.description"
            ></p>
            <p
              class="local-result-telephone"
              v-if="result.telephone"
            >
              {{ result.telephone }}
            </p>
            <a
              v-if="result.link"
              :href="result.link"
              target="_blank"
              rel="noopener noreferrer"
              class="local-result-link"
              >더 보기</a
            >
          </div>
        </div>
        <p v-else class="empty-message">
          주변 정보가 없습니다.
        </p>
      </div>
    </div>
  </div>
  <!-- Image Modal -->
  <div
    v-if="selectedImage"
    class="image-modal"
    @click="closeImageModal"
  >
    <div class="modal-content" @click.stop>
      <img
        :src="selectedImage.link"
        :alt="selectedImage.title"
        @error="handleImageError"
      />
      <p>{{ selectedImage.title }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import SearchBar from './SearchBar.vue';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { useRouter } from 'vue-router';
import api from '@/api/like/likePropertyApi';
import axiosInstance from '@/axiosInstance';
const props = defineProps({
  selectedProperty: Array,
  isDetailsVisible: Boolean, // is-details-visible을 프롭스로 정의
});
const emit = defineEmits([
  'update:selectedProperty',
  'move-map-to-coordinates', // 상위 컴포넌트로 좌표 전달을 위한 이벤트 추가
  'updateTransactionType',
  'updateBuildingType',
  'favoriteItem',
  'togglePanel',
]);
// 즐겨찾기와 상세보기 토글 상태 관리
const wishlistVisible = ref(true);
const isDetailsVisible = ref(true);
// 즐겨찾기 데이터
const wishlist = ref([]);
// 블로그/지역/이미지
const blogSearchVisible = ref(true);
const blogPosts = ref([]);
const isLoading = ref(false);
const localSearchVisible = ref(true);
const imageSearchVisible = ref(true);
const localResults = ref([]);
const imageResults = ref([]);
const isLocalLoading = ref(false);
const isImageLoading = ref(false);
const selectedImage = ref(null);
const reloadWishlist = async () => {
  try {
    const response = await api.getList();
    wishlist.value = response;
  } catch (e) {
    console.log(e);
  }
};
// 즐겨찾기 상태 관리
const isFavorite = ref(false);
// watch를 사용해 selectedProperty가 변경될 때마다 isFavorite 업데이트
watch(
  () => props.selectedProperty,
  (newValue) => {
    if (newValue && newValue.length > 0) {
      for (let wish of wishlist.value) {
        if (wish.locationNo === newValue[0].locationNo) {
          isFavorite.value = true;
        } else {
          isFavorite.value = false;
        }
      }
    } else {
      isFavorite.value = false; // 선택된 아파트가 없으면 기본값 false
    }
  },
  { immediate: true }
);
// 즐겨찾기에서 상세보기
const favoriteClick = (wish) => {
  emit('favoriteItem', wish);
};
// 즐겨찾기에서 아이템 삭제 함수
const removeFromWishlist = async (proNo) => {
  for (let wish of wishlist.value) {
    if (wish.propertyNo === proNo) {
      await api.delete(proNo);
      reloadWishlist();
    }
  }
  // 아이콘 상태 업데이트
  if (props.selectedProperty[0]?.propertyNo === proNo) {
    isFavorite.value = false; // 상세보기에서 해당 아이콘 상태 변경
  }
};
// 토글 함수
const toggleWishlist = () => {
  wishlistVisible.value = !wishlistVisible.value;
};
const toggleDetails = () => {
  isDetailsVisible.value = !isDetailsVisible.value;
};
// 즐겨찾기 상태 토글
const toggleFavorite = async () => {
  if (
    !props.selectedProperty ||
    props.selectedProperty.length === 0
  )
    return;
  if (isFavorite.value) {
    // 즐겨찾기 O -> X
    await api.delete(props.selectedProperty[0].propertyNo);
    reloadWishlist();
  } else {
    // 즐겨찾기 X -> O
    await api.create(props.selectedProperty[0].propertyNo);
    reloadWishlist();
  }
  isFavorite.value = !isFavorite.value;
};
// 아파트 선택 함수
const selectApartment = (propertyAddrAptName) => {
  const selected = props.selectedProperty.find(
    (prop) =>
      prop.propertyAddrAptName === propertyAddrAptName
  );
  if (selected) {
    emit('update:selectedProperty', [selected]);
  }
};
// 선택된 주소에 따른 좌표 처리
const handleAddressSelected = (coordinates) => {
  // 좌표를 상위 컴포넌트로 전달
  emit('move-map-to-coordinates', coordinates);
};
const router = useRouter();
const analyzeProperty = () => {
  if (
    props.selectedProperty.length > 0 &&
    props.selectedProperty[0].doroJuso
  ) {
    // selectedProperty 배열의 첫 번째 객체의 doro 값을 추출
    const jibunJuso = props.selectedProperty[0].jibunJuso;
    const buildingName =
      props.selectedProperty[0].propertyAddrAptName;
    const propertyNo = props.selectedProperty[0].propertyNo;
    const zipCode = props.selectedProperty[0].zipCode;
    const price = props.selectedProperty[0].price;
    router.push({
      name: 'mapAnalyzing',
      query: {
        jibunJuso: jibunJuso,
        buildingName: buildingName,
        propertyNo: propertyNo,
        zipCode: zipCode,
        price: price,
      },
    });
  }
};
// 블로그 검색 토글 함수
const toggleBlogSearch = () => {
  blogSearchVisible.value = !blogSearchVisible.value;
};
// 블로그 검색 함수
const searchBlogs = async (query) => {
  isLoading.value = true;
  try {
    const response = await axiosInstance.get('/blog', {
      params: {
        query: query,
        display: 5, // 표시할 블로그 포스트 수
        start: 1,
      },
    });
    blogPosts.value = response.data.items.map((item) => ({
      title: decodeHtmlEntities(stripHtmlTags(item.title)),
      link: item.link,
      description: decodeHtmlEntities(
        stripHtmlTags(item.description)
      ),
      bloggername: item.bloggername,
      postdate: item.postdate,
    }));
  } catch (error) {
    console.error('블로그 검색 중 오류 발생:', error);
    blogPosts.value = [];
  } finally {
    isLoading.value = false;
  }
};
// 토글
const toggleLocalSearch = () => {
  localSearchVisible.value = !localSearchVisible.value;
};
const toggleImageSearch = () => {
  imageSearchVisible.value = !imageSearchVisible.value;
};
const searchLocal = async (query) => {
  isLocalLoading.value = true;
  try {
    const response = await axiosInstance.get('/local', {
      params: {
        query: query,
        display: 5,
        start: 1,
        sort: 'random',
      },
    });
    // console.log('Local search response:', response.data);
    localResults.value = response.data.items.map(
      (item) => ({
        title: decodeHtmlEntities(item.title || ''),
        category: item.category || '',
        address: decodeHtmlEntities(item.address || ''),
        roadAddress: decodeHtmlEntities(
          item.roadAddress || ''
        ),
        link: item.link || '',
        mapx: item.mapx || '',
        mapy: item.mapy || '',
      })
    );
    // console.log(
    //   'Processed local results:',
    //   localResults.value
    // );
  } catch (error) {
    console.error('로컬 검색 중 오류 발생:', error);
    localResults.value = [];
  } finally {
    isLocalLoading.value = false;
  }
};
// 이미지 검색
const searchImages = async (query) => {
  isImageLoading.value = true;
  try {
    const response = await axiosInstance.get('/image', {
      params: {
        query: query,
        display: 9,
        start: 1,
        sort: 'sim',
        filter: 'large',
      },
    });
    imageResults.value = response.data.items.map(
      (item) => ({
        title: decodeHtmlEntities(
          stripHtmlTags(item.title)
        ),
        link: item.link,
        thumbnail: item.thumbnail,
      })
    );
  } catch (error) {
    console.error('이미지 검색 중 오류 발생:', error);
    imageResults.value = [];
  } finally {
    isImageLoading.value = false;
  }
};
const handleImageError = (event) => {
  event.target.src = '@/assets/nonefoundimage.png';
};
const openImageModal = (image) => {
  selectedImage.value = image;
};
const closeImageModal = () => {
  selectedImage.value = null;
};
// HTML 엔티티 디코딩 및 태그 제거 함수
const decodeHtmlEntities = (text) => {
  const textArea = document.createElement('textarea');
  textArea.innerHTML = text;
  return textArea.value;
};
const stripHtmlTags = (text) => {
  return text.replace(/<[^>]*>/g, '');
};
// 날짜 포맷팅 함수
const formatDate = (dateStr) => {
  if (!dateStr || dateStr.length < 8) return '';
  return `${dateStr.slice(0, 4)}.${dateStr.slice(
    4,
    6
  )}.${dateStr.slice(6)}`;
};
// selectedProperty 변경 감지 및 블로그 검색 실행
watch(
  () => props.selectedProperty,
  (newValue) => {
    if (newValue && newValue.length > 0) {
      const jibunJuso = newValue[0].jibunJuso;
      searchBlogs(jibunJuso);
    } else {
      blogPosts.value = [];
    }
  },
  { immediate: true }
);
watch(
  () => props.selectedProperty,
  (newValue) => {
    if (newValue && newValue.length > 0) {
      const query = newValue[0].jibunJuso;
      searchImages(query);
    } else {
      imageResults.value = [];
    }
  },
  { immediate: true }
);
watch(
  () => props.selectedProperty,
  (newValue) => {
    if (newValue && newValue.length > 0) {
      const jibunJuso = newValue[0].jibunJuso;
      const jibunAddressParts = jibunJuso.split(' ');
      const addr_dong = jibunAddressParts[1] || jibunJuso;
      searchLocal(addr_dong);
    } else {
      localResults.value = [];
    }
  },
  { immediate: true }
);
// 매매전월세 건물유형 관련
const activeFilter = ref(null); // 필터 toggle 관련
const selectedTransaction = ref('전체'); // 매매전월세 유형 디폴트값
const selectedBuilding = ref('전체'); // 건물유형 디폴트값
const toggleFilter = (filterType) => {
  activeFilter.value =
    activeFilter.value === filterType ? null : filterType;
};
const selectTransaction = (type) => {
  selectedTransaction.value = type;
  // activeFilter.value = null; // 선택이후 닫기
  emit('updateTransactionType', selectedTransaction.value);
};
const selectBuilding = (type) => {
  selectedBuilding.value = type;
  // activeFilter.value = null; // 선택이후 닫기
  emit('updateBuildingType', selectedBuilding.value);
};
onMounted(() => {
  reloadWishlist();
});
</script>

<style scoped>
.left-panel {
  width: 25%;
  height: 100%;
  padding: 24px;
  position: relative;
  overflow-y: auto;
  background-color: #ffffff;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.05);
  /* 수직 및 수평 가운데 정렬을 위한 flexbox */
  justify-content: center;
  align-items: center;
}
.panel-section {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #1a73e8;
  color: white;
  border-radius: 12px 12px 0 0;
  cursor: pointer;
  transition: background-color 0.3s ease;
}
.section-header:hover {
  background: #1557b0;
}
.header-text {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.section-content {
  padding: 20px;
  background: #fff;
  border-radius: 0 0 12px 12px;
}
.hamburger-menu {
  width: 24px;
  height: 18px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.hamburger-menu .bar {
  width: 100%;
  height: 2px;
  background-color: #fff;
  transition: all 0.3s ease;
  border-radius: 2px;
}
.hamburger-menu.active .bar:nth-child(1) {
  transform: translateY(8px) rotate(45deg);
  transform: translateY(2px) rotate(140deg); /* 첫 번째 선을 위쪽으로 향하는 대각선으로 변환 */
  width: 80%;
}
.hamburger-menu.active .bar:nth-child(2) {
  opacity: 0;
  opacity: 0;
}
.hamburger-menu.active .bar:nth-child(3) {
  transform: translateY(-8px) rotate(-45deg);
  transform: translateY(-2px) rotate(-140deg); /* 세 번째 선을 위쪽으로 향하는 반대 대각선으로 변환 */
  width: 80%;
}

.wishlist {
  list-style: none;
  padding: 0;
  margin: 0;
}
.wishlist-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.wishlist-item:hover {
  background: #e9ecef;
  transform: translateX(4px);
}
.property-header {
  text-align: center;
  margin-bottom: 24px;
}
.apart-name {
  font-size: 24px;
  color: #1a73e8;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.address {
  font-size: 14px;
  color: #666;
  margin: 0;
}
.year {
  color: #888;
  font-size: 13px;
}
.table-container {
  margin: 20px 0;
  border-radius: 8px;
  overflow: hidden;
}
.property-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: white;
}
.property-table th {
  background: #1a73e8;
  color: white;
  padding: 12px;
  font-weight: 500;
  text-align: center;
}
.property-table td {
  padding: 12px;
  text-align: center;
  border-bottom: 1px solid #e0e0e0;
  color: #333;
}
.property-table tbody tr:hover {
  background-color: #f8f9fa;
}
.empty-message {
  text-align: center;
  color: #666;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}
.analyze-button-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
.analyze-button {
  background: #1a73e8;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(26, 115, 232, 0.2);
}
.analyze-button:hover {
  background: #1557b0;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(26, 115, 232, 0.3);
}
.favorite-icon {
  cursor: pointer;
  transition: transform 0.2s ease;
}
.favorite-icon:hover {
  transform: scale(1.2);
}
.search {
  display: flex;
  flex-direction: column;
  gap: 16px; /* 요소 간의 간격 */
  justify-content: space-between;
  align-items: stretch;
  margin-bottom: 24px;
  width: 100%; /* panel-section과 동일한 width 적용 */
}

.blog-search-toggle {
  margin-top: 24px;
}
.blog-posts {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.blog-post {
  background: #f8f9fa;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s ease;
}
.blog-post:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
.blog-post-link {
  display: block;
  padding: 16px;
  text-decoration: none;
  color: inherit;
}
.blog-post-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a73e8;
  margin-bottom: 8px;
}
.blog-post-description {
  font-size: 14px;
  color: #555;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.blog-post-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #888;
}
.loading-spinner {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
}
.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1a73e8;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
}
.local-results,
.image-results {
  display: grid;
  gap: 16px;
}
.local-result {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.2s ease;
}
.local-result:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}
.local-result-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a73e8;
  margin-bottom: 8px;
}
.local-result-category,
.local-result-address,
.local-result-description {
  font-size: 14px;
  color: #555;
  margin-bottom: 4px;
}
.image-results {
  grid-template-columns: repeat(3, 1fr);
}
.image-result img {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s ease;
}
.image-result img:hover {
  transform: scale(1.05);
}
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}
.modal-content {
  background: white;
  padding: 16px;
  border-radius: 8px;
  max-width: 90%;
  max-height: 90%;
  overflow: auto;
}
.modal-content img {
  max-width: 100%;
  height: auto;
}
.modal-content p {
  margin-top: 8px;
  font-size: 14px;
  color: #333;
  text-align: center;
}
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 검색 영역 반응형 조정 */
@media (max-width: 768px) {
  .left-panel {
    width: 100%;
    height: auto;
  }

  /* 작은 화면에서는 검색 영역이 줄어들도록 조정 */
  .search {
    flex-direction: column;
    align-items: stretch;
  }

  .search button {
    width: 100%;
  }
}
</style>
