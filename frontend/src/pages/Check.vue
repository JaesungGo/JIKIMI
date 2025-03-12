<template>
  <div class="address-search-container">
    <input
      type="button"
      @click="openDaumPostcode"
      value="단지, 지역, 지하철, 학교 검색"
      class="search-input"
      :disabled="isLoading"
    />

    <div v-if="isLoading" class="loading-spinner">
      검색 중...
    </div>

    <!-- 주소 입력 폼 -->
    <div v-if="selectedAddress && showAddressForm" class="selected-address-form">
      <h3>주소 입력</h3>
      <div class="form-group">
        <label>도로명 주소:</label>
        <input 
          type="text" 
          :value="selectedAddress.roadAddress" 
          readonly 
          class="form-control"
        />
      </div>
      
      <div class="form-group">
        <label>건물명:</label>
        <input 
          type="text" 
          :value="selectedAddress.buildingName" 
          readonly 
          class="form-control"
        />
      </div>

      <div class="residence-type">
        <p>아파트 / 오피스텔에 거주 중인가요?</p>
        <div class="button-group">
          <button 
            @click="handleResidenceType('yes')"
            :class="['option-button', { active: residenceType === 'yes' }]"
          >
            예
          </button>
          <button 
            @click="handleResidenceType('no')"
            :class="['option-button', { active: residenceType === 'no' }]"
          >
            아니오
          </button>
        </div>
      </div>

      <!-- 아파트/오피스텔인 경우 동/호수 입력 폼 -->
      <div v-if="residenceType === 'yes'" class="detail-form">
        <form @submit.prevent="submitForm">
          <div class="form-group">
            <label>동:</label>
            <input 
              type="text" 
              v-model="dong" 
              required 
              class="form-control"
              pattern="[0-9]{1,4}"
              title="1-4자리 숫자만 입력 가능합니다"
            />
          </div>
          <div class="form-group">
            <label>호수:</label>
            <input 
              type="text" 
              v-model="ho" 
              required 
              class="form-control"
              pattern="[0-9]{1,4}"
              title="1-4자리 숫자만 입력 가능합니다"
            />
          </div>
          <button type="submit" class="submit-button" :disabled="isLoading">
            제출
          </button>
        </form>
      </div>

      <!-- 아파트/오피스텔이 아닌 경우 바로 제출 버튼 -->
      <div v-if="residenceType === 'no'" class="submit-section">
        <button 
          @click="submitForm"
          class="submit-button"
          :disabled="isLoading"
        >
          제출
        </button>
      </div>
    </div>

    <!-- 유니크 코드 목록 -->
    <div v-if="addresses.length && !showAddressForm" class="address-list">
      <h3>유니크 코드 목록</h3>
      <div v-for="address in addresses" :key="address.commonUniqueNo" class="address-item">
        <div class="address-details">
          <p class="unique-code">유니크 코드: {{ address.commonUniqueNo }}</p>
          <p class="address">주소: {{ address.commAddrLotNumber }}</p>
          <p class="status">상태: {{ address.resState }}</p>
        </div>
        <button 
          @click="sendUniqueCode(address.commonUniqueNo)"
          class="select-button"
          :disabled="isLoading"
        >
          선택
        </button>
      </div>
      
      <!-- 새 주소 검색 버튼 -->
      <button 
        @click="resetForm(true)" 
        class="new-search-button"
      >
        새로운 주소 검색
      </button>
    </div>

    <div v-if="errorMessage" class="error-message" role="alert">
      {{ errorMessage }}
      <button @click="errorMessage = ''" class="close-error">✕</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axiosInstance from '../axiosInstance';

// 상태 관리
const addresses = ref([]);
const selectedAddress = ref(null);
const dong = ref('');
const ho = ref('');
const errorMessage = ref('');
const isLoading = ref(false);
const residenceType = ref(null);
const showAddressForm = ref(false);

// 에러 처리 함수
const handleError = (error, customMessage) => {
  console.error(error);
  errorMessage.value = customMessage || '처리 중 오류가 발생했습니다. 다시 시도해 주세요.';
  isLoading.value = false;
};

// Daum 우편번호 검색
const openDaumPostcode = () => {
  new daum.Postcode({
    oncomplete: (data) => {
      selectedAddress.value = {
        zonecode: data.zonecode,
        roadAddress: data.roadAddress,
        buildingName: data.buildingName || '없음',
        apartment: data.apartment,
        jibunAddress: data.autoJibunAddress,
        bname: data.bname,
        sido: data.sido 
      };
      showAddressForm.value = true;
      addresses.value = []; // 주소 선택 시 이전 유니크 코드 목록 초기화
      resetForm(false);
    },
  }).open();
};

// 거주 유형 선택 처리
const handleResidenceType = (type) => {
  residenceType.value = type;
  if (type === 'no') {
    dong.value = '';
    ho.value = '';
  }
};

// 폼 제출
const submitForm = async () => {
  if (isLoading.value) return;
  
  isLoading.value = true;
  try {
    console.log('jibun before:', selectedAddress.value.jibunAddress);
    const lotNumberParts = selectedAddress.value.jibunAddress.split(' ');
    const lotNumber = lotNumberParts[lotNumberParts.length - 1];

    const payload = {
      addr_sido: selectedAddress.value.sido || "",
      addr_dong: selectedAddress.value.bname || "",
      addr_lotNumber: lotNumber,
      buildingName: selectedAddress.value.buildingName || "",
      dong: residenceType.value === 'yes' ? dong.value : "",
      ho: residenceType.value === 'yes' ? ho.value : "",
      realtyType: residenceType.value === 'yes' ? 1 : 0
    };

    const response = await axiosInstance.post('/safety-inspection/address', payload);
    if (Array.isArray(response.data) && response.data.length > 0) {
      addresses.value = response.data;
      showAddressForm.value = false;
      selectedAddress.value = null;
      dong.value = '';
      ho.value = '';
    } else {
      errorMessage.value = '조회된 주소가 없습니다.';
    }
  } catch (error) {
    handleError(error, '주소 검증에 실패했습니다.');
  } finally {
    isLoading.value = false;
  }
};

// 유니크 코드 전송
const sendUniqueCode = async (uniqueCode) => {
  if (isLoading.value) return;
  
  isLoading.value = true;
  try {
    const response = await axiosInstance.post('/safety-inspection/unique-code', {
      uniqueCode: uniqueCode
    });
    // 성공 처리 로직
  } catch (error) {
    handleError(error, '유니크 코드 전송에 실패했습니다.');
  } finally {
    isLoading.value = false;
  }
};

// 초기화 함수
const resetForm = (fullReset = true) => {
  if (fullReset) {
    selectedAddress.value = null;
    showAddressForm.value = false;
    addresses.value = [];
  }
  dong.value = '';
  ho.value = '';
  errorMessage.value = '';
};

onMounted(() => {
  resetForm(true);
});
</script>


<style scoped>
.address-search-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.search-input {
  width: 100%;
  height: 60px;
  border-radius: 10px;
  border: 1px solid #ccc;
  padding: 0 15px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-input:hover:not(:disabled) {
  border-color: rgb(0, 181, 0);
}

.search-input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.loading-spinner {
  text-align: center;
  margin: 20px 0;
  color: #666;
}

.address-list {
  margin-top: 20px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #eee;
  margin: 10px 0;
  padding: 15px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.address-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.select-button {
  background-color: rgb(0, 181, 0);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.select-button:hover:not(:disabled) {
  background-color: rgb(0, 150, 0);
}

.select-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.form-group {
  margin-bottom: 15px;
}

.form-control {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.error-message {
  background-color: #fff3f3;
  color: #dc3545;
  padding: 10px;
  border-radius: 4px;
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-error {
  background: none;
  border: none;
  color: #dc3545;
  cursor: pointer;
}

.button-group {
  display: flex;
  gap: 10px;
  margin: 10px 0;
}

.option-button {
  padding: 8px 16px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: white;
  cursor: pointer;
}

.option-button.active {
  background-color: rgb(0, 181, 0);
  color: white;
  border-color: rgb(0, 181, 0);
}

.submit-button {
  background-color: rgb(0, 181, 0);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  width: 100%;
  margin-top: 10px;
}

.submit-button:hover:not(:disabled) {
  background-color: rgb(0, 150, 0);
}

.submit-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.submit-section {
  margin-top: 20px;
}

.new-search-button {
  background-color: #666;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  width: 100%;
  margin-top: 20px;
  transition: background-color 0.3s ease;
}

.new-search-button:hover {
  background-color: #555;
}

.address-list {
  margin-top: 20px;
  padding: 20px;
  border-radius: 8px;
  background-color: #f8f9fa;
}
</style>