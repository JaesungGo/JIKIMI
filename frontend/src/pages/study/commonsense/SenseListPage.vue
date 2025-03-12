<template>
  <div class="container mt-4">
    <!-- ====== Page Title Section Start -->
    <section class="dark:bg-dark bg-white py-[100px]">
      <div class="mx-0 px-0 sm:container">
        <div class="border-primary border-l-[5px] pl-5">
          <h4 class="text-dark mb-2 text-4xl font-semibold dark:text-white">
            부동산 토막 상식
          </h4>
          <p class="text-body-color dark:text-dark-6 text-xl font-medium">
            반드시 알아야 할 상식들을 준비했어요! 상식 학습을 통해 안전 거래를
            진행하세요!
          </p>
        </div>
      </div>
    </section>
    <!-- ====== Page Title Section End -->
    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="text-center my-4">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
    </div>
    <!-- 에러 메시지 -->
    <div v-else-if="errorMessage" class="alert alert-danger" role="alert">
      {{ errorMessage }}
    </div>
    <!-- 게시글 목록 그리드 -->
    <div v-else class="sense-grid">
      <div
        v-for="article in articles"
        :key="article.commonSenseNo"
        class="sense-item"
        @click="detail(article.commonSenseNo)"
      >
        <div class="sense-content">
          <div class="sense-title">{{ article.pieceSense }}</div>
        </div>
      </div>
    </div>

    <!-- Modal for Detail Page -->
    <div v-if="showModal" class="modal" @click.self="closeModal">
      <div class="modal-content">
        <span class="close" @click="closeModal">&times;</span>
        <h2>{{ selectedArticle?.pieceSense }}</h2>
        <!-- 선택한 기사의 제목 -->
        <div class="detail-content">
          {{ selectedArticle?.commonSenseContent }}
        </div>
        <!-- 선택한 기사의 내용 -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/senseApi'; // API 모듈

const router = useRouter();
// 상태 관리
const articles = ref([]);
const isLoading = ref(true);
const errorMessage = ref('');
const showModal = ref(false); // 모달 상태 관리
const selectedArticle = ref(null); // 선택한 기사 상태

// 게시글 상세 보기 (모달로 열기)
const detail = (no) => {
  const article = articles.value.find((item) => item.commonSenseNo === no);
  if (article) {
    selectedArticle.value = article; // 선택한 기사 저장
    console.log('Selected Article:', selectedArticle.value); // 선택한 기사 확인
    showModal.value = true; // 모달 열기
  }
};

// 모달 닫기
const closeModal = () => {
  showModal.value = false;
  selectedArticle.value = null; // 선택한 기사 초기화
};

// 데이터 로드 함수
const load = async () => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const response = await api.getList({ page: 1, amount: 10 }); // 큰 수를 지정하여 모든 항목을 가져옵니다
    articles.value = response.list;
  } catch (error) {
    console.error('게시글 로드 실패:', error);
    errorMessage.value =
      '게시글을 불러오는 데 실패했습니다. 다시 시도해 주세요.';
  } finally {
    isLoading.value = false;
  }
};

// 컴포넌트가 마운트될 때 데이터 로드
onMounted(() => {
  load();
});
</script>

<style scoped>
.card-title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 40px;
}

/* 전체 섹션의 좌측 여백 통일 */
.container {
  max-width: 1500px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-container {
  padding-left: 20px; /* 타이틀과 다른 컨텐츠의 시작점을 통일 */
}
.sense-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.sense-item {
  background-color: white;
  border: 2px solid #b8d8ff; /* 보더 너비와 스타일 추가 */
  border-radius: 25px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease,
    background-color 0.3s ease;
  cursor: pointer;
  aspect-ratio: 1 / 1;
  padding: 10px; /* 패딩 추가 */
  position: relative; /* 숫자를 위치시키기 위해 필요 */
}

.sense-item:hover {
  transform: scale(1.05);
  background-color: #f0f7ff; /* Sky blue color on hover */
}

.sense-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  text-align: center;
}

.sense-number {
  font-size: 14px; /* 숫자 크기 감소 */
  font-weight: bold;
  position: absolute; /* 좌측 상단으로 이동시키기 위해 추가 */
  top: 10px;
  left: 10px;
}

.sense-title {
  font-size: 20px; /* 제목 크기 증가 */
  font-weight: bold;
  margin-top: 20px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

/* Modal styles */
/* Modal styles */
.modal {
  display: flex;
  position: fixed;
  z-index: 1;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: rgba(0, 0, 0, 0.4);
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: white; /* 배경색을 흰색으로 */
  width: 30rem; /* 카드 너비 설정 */
  padding: 1.5rem; /* 내부 패딩 추가 */
  border-radius: 0.5rem; /* 카드 모서리 둥글게 */

  position: relative; /* 상대 위치 지정 */
}

.modal-content:hover {
  margin-top: -0.5rem; /* hover 시 카드 위로 이동 */
}

.close {
  position: absolute;
  right: 10px;
  top: 10px;
  font-size: 1.5rem;
  cursor: pointer;
}

.close:hover,
.close:focus {
  color: #000;
  text-decoration: none;
  cursor: pointer;
}

h2 {
  margin: 0;
  font-size: 1.5rem; /* 제목 폰트 크기 증가 */
  font-weight: bold;
  margin-bottom: 1rem; /* 제목과 내용 간격 조정 */
}

.detail-content {
  white-space: pre-line;
  font-size: 1rem; /* 내용 폰트 크기 */
  line-height: 1.6; /* 줄 간격 조정 */
  margin-bottom: 1rem;
}

.button-container {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}
@media (max-width: 768px) {
  .sense-grid {
    grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
    gap: 15px;
  }

  .sense-number {
    font-size: 12px;
  }

  .sense-title {
    font-size: 16px;
  }
}

@media (min-width: 1024px) {
  .sense-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }

  .sense-number {
    font-size: 16px;
  }

  .sense-title {
    font-size: 22px; /* 데스크탑에서는 더 크게 설정 */
  }
}
</style>
