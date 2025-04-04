<template>
  <!-- ====== Hero Section Start -->
  <div class="hero-section">
    <!-- 왼쪽 섹션 -->
    <div class="left-section">
      <div class="hero-compo">
        <div class="hero-content">
          <h1 class="hero-title">내 집은<br />안전할까?</h1>
        </div>

        <p class="hero-description">
          계약 진행 단계에 따라 반드시 필요한 <br />체크리스트와 발생 가능
          시나리오를 준비했어요. <br />지금 바로 확인해보세요!
        </p>
        <ul class="button-list">
          <li>
            <router-link :to="{ name: 'preventionList' }" class="button-primary"
              >계약이 처음이라면?</router-link
            >
          </li>
          <li>
            <router-link :to="{ name: 'ScenarioMain' }" class="button-secondary"
              >계약 진행/완료 상태라면!</router-link
            >
          </li>
        </ul>
      </div>
      <div class="swiper">
        <div class="piece-sense-container">
          <div class="scroll-container" ref="scrollContainer">
            <div class="scroll-wrapper">
              <div
                v-for="article in articles"
                :key="article.commonSenseNo"
                class="card"
              >
                <PieceSenseCard
                  :commonSenseTitle="article.commonSenseTitle"
                  :commonSenseNo="article.commonSenseNo"
                  :commonSenseContent="article.commonSenseContent"
                  :commonPieceSense="article.pieceSense"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="right-section">
      <img src="@/assets/map.png" alt="hero" class="map-image" />
      <p class="map-description">
        지도를 통해 원하시는 지역의 시세를 확인하고,<br />
        등기부 등본 분석을 통해 안전 진단 리포트를 제공해 드릴게요.
      </p>
      <router-link :to="{ name: 'map' }" class="button-primary right-button"
        >안전 진단 받기</router-link
      >
    </div>
  </div>

  <Footer></Footer>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed, reactive } from 'vue'; // 한 번에 ref, onMounted 가져오기
import { useRouter } from 'vue-router'; // Vue Router 사용
import useAuthStore from '@/stores/auth'; // 인증 스토어 가져오기
import api from '@/api/senseApi';
import PieceSenseCard from '@/components/Cards/PieceSenseCard.vue';
import Footer from '@/components/Footer.vue';

const open = ref(false);
const dropdownButtonRef = ref<HTMLButtonElement | null>(null);
const router = useRouter(); // 라우터 사용
const isNavbarOpen = ref(false); // 햄버거 메뉴 열림 상태
const isDropdownOpen = ref(false); // 드롭다운 열림 상태

const toggleNavbar = () => {
  isNavbarOpen.value = !isNavbarOpen.value;
};
// 외부 클릭 감지
const handleClickOutside = (event: MouseEvent) => {
  if (
    dropdownButtonRef.value &&
    !dropdownButtonRef.value.contains(event.target as Node)
  ) {
    open.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

// 상태 관리: 인증 여부
const authStore = useAuthStore();
const isAuthenticated = computed(() => authStore.isAuthenticated);

// 로그아웃 기능
const logout = () => {
  authStore.logout();
};

// 클라이언트 로고 정보
const clients = ref([]);

// senseApi를 통해 데이터를 로드할 함수 (API 정의 필요)
const pieceSenseList = ref([]); // API에서 받은 데이터를 저장할 곳

// 상세 페이지로 이동하는 함수
const detail = (no: number) => {
  router.push({
    name: 'senseDetailPage',
    params: { no: no },
  });
};

// 컴포넌트가 마운트될 때 데이터 로드
onMounted(() => {
  load();
});

// 스크롤 관련 상태 변수들
const scrollContainer = ref<HTMLElement | null>(null);
const isScrolling = ref(false);
let scrollInterval: ReturnType<typeof setInterval> | null = null;

// articles computed 속성 수정
const articles = computed(() => state.page.list);

// 스크롤 관련 로직 통합
const handleScroll = () => {
  if (scrollContainer.value && !isScrolling.value) {
    const container = scrollContainer.value;
    const cardWidth = container.querySelector('.card')?.clientWidth || 0;

    isScrolling.value = true;

    if (container.scrollLeft >= container.scrollWidth - container.clientWidth) {
      // 마지막에 도달했을 때 추가 데이터 로드
      if (state.page.list.length < state.page.totalCount) {
        load();
      }
      container.scrollTo({ left: 0, behavior: 'smooth' });
    } else {
      container.scrollBy({ left: cardWidth, behavior: 'smooth' });
    }

    setTimeout(() => {
      isScrolling.value = false;
    }, 500);
  }
};

const startAutoScroll = () => {
  if (scrollInterval) return;
  scrollInterval = setInterval(handleScroll, 5000); // handleScroll 함수 사용
};

// 상태 관리 개선
const state = reactive({
  isLoading: false,
  errorMessage: '',
  page: {
    list: [] as any[],
    totalCount: 0
  },
  pageRequest: {
    page: 1,
    amount: 10
  }
});

// 데이터 로드 함수 최적화
const load = async () => {
  if (state.isLoading) return;
  
  state.isLoading = true;
  state.errorMessage = '';
  
  try {
    const response = await api.getList({
      page: state.pageRequest.page,
      amount: state.pageRequest.amount,
    });
    
    // 새로운 데이터를 기존 리스트에 추가하지 않고 교체
    state.page.list = response.list;
    state.page.totalCount = response.totalCount;
  } catch (error) {
    state.errorMessage = '게시글을 불러오는 데 실패했습니다.';
    console.error('Error loading articles:', error);
  } finally {
    state.isLoading = false;
  }
};

// 모든 초기화 로직을 하나의 함수로 통합
const initialize = () => {
  // 외부 클릭 이벤트 리스너 등록
  document.addEventListener('click', handleClickOutside);
  
  // 데이터 로드
  load();
  
  // 자동 스크롤 시작
  startAutoScroll();
};

// cleanup 로직 통합
const cleanup = () => {
  // 외부 클릭 이벤트 리스너 제거
  document.removeEventListener('click', handleClickOutside);
  
  // 자동 스크롤 정지
  stopAutoScroll();
};

// 단일 onMounted와 onUnmounted 사용
onMounted(() => {
  initialize();
});

onUnmounted(() => {
  cleanup();
});
</script>

<style scoped>
/* 네비게이션 바의 선 제거 */
.container,
.flex,
.header,
nav,
ul,
li,
div,
a {
  border: none !important;
}

/* 버튼 등의 추가적인 요소들에 대한 선 제거 */
button,
.block,
.py-3,
.px-7,
.px-4,
.rounded-md,
.bg-primary {
  border: none !important;
}

/* 기타 가능한 요소들에 대한 선 제거 */
.hero-content,
.clients,
.bg-white,
.bg-gray-100 {
  border: none !important;
}

/* 텍스트와 이미지의 경계에도 선 제거 */
.text-dark,
.text-body-color,
img {
  border: none !important;
}

/* 불필요한 하단의 border 관련된 다른 클래스 제거 */
.lg:px-12,
.lg:pr-0,
.rounded-md {
  border: none !important;
}

/* 메뉴 토글 버튼 (모바일용) */
.menu-toggle {
  display: none;
  font-size: 24px;
  cursor: pointer;
  background: none;
  border: none;
}

/* 기본 스타일 */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.map-container {
  display: flex; /* Flexbox 사용 */
  flex-direction: column; /* 수직 방향 정렬 */
  align-items: center; /* 수평 중앙 정렬 */
  justify-content: center; /* 수직 중앙 정렬 */
  height: 100%; /* 컨테이너의 높이를 100%로 설정 */
  text-align: center; /* 텍스트를 가운데 정렬 */
  gap: 1rem; /* 요소들 사이의 간격 추가 */
}

/* 토막 바 스타일 */

.piece-sense-title {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 1rem;
}

.scroll-container {
  width: 100%;
  overflow-y: visible; /* 수직 방향으로 카드가 잘리지 않도록 설정 */
  position: relative;
}

/* 애니메이션 정의 */
@keyframes scroll {
  0% {
    transform: translateX(0); /* 시작 위치 */
  }
  100% {
    transform: translateX(-100%); /* 끝 위치 */
  }
}

.card {
  flex: 0 0 380px; /* 카드의 고정된 크기 */
  scroll-snap-align: start;
  background-color: transparent; /* 카드 배경을 투명하게 */
  border: none; /* 경계선 제거 */
  padding: 0;
  margin: 0;
  box-shadow: none !important; /* 그림자 제거 */
  height: auto;
  /* min-height: 120%;  */

  /* flex: 0 0 auto; /* 카드가 한 줄로 수평 정렬되도록 설정 */
  /* max-width: 380px; 카드의 최대 너비 */
}

/* 네비게이션 바 스타일 */

.logo {
  flex-grow: 1;
}

.space {
  flex-grow: 5;
}
.container {
  width: 100%;
}
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%; /* 화면 너비 전체 */
  background-color: white;
  z-index: 1000;
}

.navbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  width: 1600px;
}

.logo img {
  height: 40px;
}

.menu-toggle {
  display: none;
}

.nav-menu {
  display: flex;
  align-items: center;
  flex-grow: 10;
}

.nav-list {
  display: flex;
  list-style: none;
}

.nav-item {
  margin: 0 1rem;
  color: #333;
  text-decoration: none;
}

.auth-menu {
  justify-content: flex-end; /* 우측 정렬 */

  display: flex;
  align-items: center;
  text-align: right;
  margin-left: auto; /* 우측으로 밀기 */
  flex-grow: 1;
}

.auth-item {
  padding: 10px 20px;
  border-radius: 5px;
  text-decoration: none;
  color: #333;
  justify-content: space-between;
}

/* Hero 섹션 스타일 */
.hero-section {
  display: flex;
  max-height: 100vh;
  padding-top: 0; /* 헤더 높이 + 여백 */
  padding-bottom: 20px; /* 하단에 약간의 여백 추가 */
  align-items: stretch; /* 좌우 섹션의 높이를 동일하게 설정 */
}
.hero-compo {
  margin-left: 6rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  flex-grow: 1; /* 남은 공간을 채우도록 설정 */
}

.piece-sense-container {
  margin-top: auto;
  width: 150%;
  overflow: visible; /* 컨테이너를 넘어서는 내용도 표시 */
  margin-left: -6rem; /* 왼쪽 여백을 제거하기 위해 음수 마진 적용 */
  margin-right: 2rem; /* 오른쪽 여백을 제거하기 위해 음수 마진 적용 */
}
.scroll-container {
  width: 100%;
  overflow-x: hidden;
  overflow-y: visible;
  padding-bottom: 20px; /* 하단에 여유 공간 추가 */
}

.scroll-wrapper {
  display: flex;
  animation: scroll 30s linear infinite;
}

.swiper {
  margin-left: 0;
  margin-right: 0; /* 오른쪽 여백도 제거 */
  padding: 0;
  width: 100%; /* 전체 너비를 사용하도록 설정 */
}

.left-section,
.right-section {
  flex: 1;
  padding: 2rem;
  display: flex;
  flex-direction: column;
}

.left-section {
  background-color: #f0f7ff; /* 밝은 회색 배경 추가 */
  border-bottom-right-radius: 80px; /* 우측 하단 모서리 둥글게 */
  width: 50%; /* 전체 화면의 50% */
  overflow: hidden; /* 내부 요소가 섹션을 넘지 않도록 설정 */
  justify-content: space-between; /* 내용을 위아래로 분산 */
  /* min-height: 100vh; /* 화면의 높이를 100%로 설정 */
}
.right-section {
  background-color: transparent;
  display: flex;
  justify-content: center; /* 가로 중앙 정렬 */
  align-items: center; /* 세로 중앙 정렬 */
  flex-direction: column; /* 요소들을 세로로 쌓음 */
  text-align: center; /* 텍스트 중앙 정렬 */
}

.hero-title {
  font-size: 3.5rem;
  margin-bottom: 0.5rem;
}

.hero-description {
  margin-bottom: 2rem;
}

.hero-description,
.map-description {
  font-size: 1.2rem;
  line-height: 1.5;
}

.button-list {
  list-style: none;
  display: flex;
  gap: 1rem;

  margin-bottom: 1rem;
}

.button-primary,
.button-secondary,
.right-button {
  padding: 0.75rem 1.5rem;
  border-radius: 0.375rem;
  text-decoration: none;
  display: inline-block;
  font-size: 1rem;
  min-width: 200px;
  text-align: center;
  margin-top: 1rem; /* 버튼과 설명 텍스트 사이의 간격 추가 */
}

.button-secondary {
  border: 2px solid #0066cc; /* 테두리 두께와 색상 설정 */
  color: #0066cc; /* 글자 색상 설정 */
  background-color: white; /* 배경색을 투명하게 설정 */
  padding: 0.75rem 1.5rem; /* 패딩 추가 */
  border-radius: 0.375rem; /* 모서리를 둥글게 설정 */
  text-decoration: none; /* 링크의 기본 밑줄 제거 */
  display: inline-block; /* 버튼처럼 보이도록 블록 설정 */
  transition: background-color 0.3s ease, color 0.3s ease; /* 배경과 텍스트 색상 전환 효과 */
}

.button-secondary:hover {
  background-color: #0066cc; /* 마우스를 올렸을 때 배경색 변경 */
  color: white; /* 마우스를 올렸을 때 텍스트 색상 변경 */
}

.button-primary {
  background-color: #0066cc;
  color: white;
}

.right-button {
  justify-content: center;
}

.button-secondary {
  border: 1px solid #0066cc;
  color: #0066cc;
}

.map-image {
  max-width: 100%;
  margin-bottom: 1rem;
}

/* 추가적인 스타일 개선 */
.left-section {
  justify-content: flex-start;
  padding-top: 2rem;
}

/* Responsive styles */
@media (max-width: 768px) {
  .hero-section {
    flex-direction: column;
    padding-top: 60px;
  }

  .left-section,
  .right-section {
    width: 100%;
    padding: 1rem;
  }

  .hero-compo {
    margin-left: 0;
    align-items: center;
    text-align: center;
  }

  .piece-sense-container {
    margin-left: 0;
    margin-right: 0;
    width: 100%;
  }

  .scroll-container {
    overflow-x: hidden;
    padding-bottom: 20px;
  }

  .scroll-wrapper {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .card {
    flex: 0 0 85%;
    max-width: 85%;
    margin-right: 1rem;
  }

  .button-list {
    flex-direction: column;
    width: 100%;
  }

  .button-primary,
  .button-secondary {
    width: 100%;
    margin-bottom: 1rem;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .hero-description {
    font-size: 1rem;
  }

  .left-section {
    border-bottom-right-radius: 0;
    overflow: visible;
  }
}
</style>
