<template>
  <div class="container mt-5">
    <h1>3.14159265358979</h1>
    <form @submit.prevent="test">
      <!-- COR -->
      <h1 class="text-center mt-5">COR</h1>
      <div class="form-group">
        <label>analysisNo:</label>
        <input type="text" v-model="analysisNo" class="form-control" />
      </div>
      <div class="form-group">
        <label>ownerState(전유부분):</label>
        <input type="text" v-model="ownerState" class="form-control" />
      </div>
      <div class="form-group">
        <label>ownership(소유자):</label>
        <input type="text" v-model="ownership" class="form-control" />
      </div>
      <div class="form-group">
        <label>commonOwner(공동소유/단독소유 여부):</label>
        <input type="text" v-model="commonOwner" class="form-control" />
      </div>
      <div class="form-group">
        <label>changeOwnerCount(소유자 바뀐 횟수):</label>
        <input type="text" v-model="changeOwnerCount" class="form-control" />
      </div>
      <div class="form-group">
        <label>maximumOfBond(채권최고액):</label>
        <input type="text" v-model="maximumOfBond" class="form-control" />
      </div>

      <!-- BML -->
      <h1 class="text-center mt-5">BML</h1>
      <div class="form-group">
        <label>resViolationStatus(위반건축물 여부):</label>
        <input type="text" v-model="resViolationStatus" class="form-control" />
      </div>
      <div class="form-group">
        <label>resContents(주용도):</label>
        <input type="text" v-model="resContents" class="form-control" />
      </div>

      <!-- payload -->
      <h1 class="text-center mt-5">payload</h1>
      <div class="form-group">
        <label>addr_sido(시/도):</label>
        <input type="text" v-model="addr_sido" class="form-control" />
      </div>
      <div class="form-group">
        <label>addr_dong(동):</label>
        <input type="text" v-model="addr_dong" class="form-control" />
      </div>
      <div class="form-group">
        <label>addr_lotNumber(지번):</label>
        <input type="text" v-model="addr_lotNumber" class="form-control" />
      </div>
      <div class="form-group">
        <label>buildingName(건물명):</label>
        <input type="text" v-model="buildingName" class="form-control" />
      </div>
      <div class="form-group">
        <label>dong(건물동):</label>
        <input type="text" v-model="dong" class="form-control" />
      </div>
      <div class="form-group">
        <label>ho(건물호):</label>
        <input type="text" v-model="ho" class="form-control" />
      </div>
      <div class="form-group">
        <label>zipcode(우편번호):</label>
        <input type="text" v-model="zipcode" class="form-control" />
      </div>
      <div class="form-group">
        <label>jeonsePrice(전세금):</label>
        <input type="text" v-model="jeonsePrice" class="form-control" />
      </div>

      <div class="form-group">
        <label>집주인 성명:</label>
        <div
          v-for="(name, index) in names"
          :key="index"
          class="name-input-container"
        >
          <input
            type="text"
            v-model="names[index]"
            required
            class="form-control"
            placeholder="집주인 성명"
          />
          <div class="button-group">
            <button @click="addName(index)" type="button" class="add-button">
              +
            </button>
            <button
              v-if="index !== 0"
              @click="removeName(index)"
              type="button"
              class="remove-button"
            >
              -
            </button>
          </div>
        </div>
      </div>
      <div class="form-group">
        <label>jibunAddress(지번주소):</label>
        <input type="text" v-model="jibunAddress" class="form-control" />
      </div>
      <div class="form-group">
        <label>propertyNo :</label>
        <input type="text" v-model="propertyNo" class="form-control" />
      </div>
      <div class="form-group">
        <label>price(매매가):</label>
        <input type="text" v-model="price" class="form-control" />
      </div>
    </form>
    <button class="btn btn-danger mt-5 ml-4" @click="test">TEST</button>
  </div>
  <h1>{{ result.totalScore }}</h1>
</template>

<script setup>
import { ref, reactive } from 'vue';
import testApi from '@/api/testApi';

// cor
const analysisNo = ref('');
const ownerState = ref('');
const ownership = ref('');
const commonOwner = ref('');
const changeOwnerCount = ref('');
const maximumOfBond = ref('');
const names = ref(['']);

// bml
const resViolationStatus = ref('');
const resContents = ref('');

const addName = () => {
  names.value.push('');
};

// 계약자 제거 함수
const removeName = (index) => {
  if (names.value.length > 1) {
    names.value.splice(index, 1);
  }
};

// payload
const addr_sido = ref('');
const addr_dong = ref('');
const addr_lotNumber = ref('');
const buildingName = ref('');
const dong = ref('');
const ho = ref('');
const zipcode = ref('');
const jeonsePrice = ref('');
const contractName = ref('');
const jibunAddress = ref('');
const propertyNo = ref('');
const price = ref('');

// result
const result = reactive({});

// test 제출
const test = async () => {
  // cor 저장
  const corData = {
    analysisNo: Number(emptyToNull(analysisNo.value)),
    ownerState: Number(emptyToNull(ownerState.value)),
    ownership: emptyToNull(ownership.value),
    commonOwner: emptyToNull(commonOwner.value),
    changeOwnerCount: emptyToNull(Number(changeOwnerCount.value)),
    maximumOfBond: emptyToNull(Number(maximumOfBond.value)),
  };

  // bml 저장

  const bmlData = {
    analysisNo: Number(emptyToNull(analysisNo.value)),
    resViolationStatus:
      emptyToNull(resViolationStatus.value) === null
        ? null
        : Boolean(Number(emptyToNull(resViolationStatus.value))),
    resContents: emptyToNull(resContents.value),
  };

  // analysis

  let checkEmpty = false;
  const namesList = contractName.value.split(' '); // 공백을 기준으로 분리
  for (let name of namesList) {
    if (name == '') {
      checkEmpty = true;
      break;
    }
  }

  const payload = {
    propertyNo: emptyToNull(propertyNo.value),
    analysisNo: Number(emptyToNull(analysisNo.value)),
    addrSido: emptyToNull(addr_sido.value),
    addrDong: emptyToNull(addr_dong.value),
    addrLotNumber: emptyToNull(addr_lotNumber.value),
    buildingName: emptyToNull(buildingName.value),
    dong: emptyToNull(dong.value),
    ho: emptyToNull(ho.value),
    zipcode: emptyToNull(zipcode.value),
    jeonsePrice: emptyToNull(jeonsePrice.value),
    contractName: names.value.length ? names.value : null,
    jibunAddress: emptyToNull(jibunAddress.value),
    price: emptyToNull(String(price.value)),
    analysisDate: String(new Date().toLocaleDateString('kr')),
  };

  console.log('payload : ', payload);

  try {
    const response = await testApi.createCor(corData);
    console.log('corData complete : ', response);
    const response2 = await testApi.createBml(bmlData);
    console.log('bmlData complete : ', response2);
    const result = await testApi.analysis(payload);
    console.log('analyzing complete : ', result);
  } catch (e) {
    console.log(e);
  }

  console.log('result : ', result);
};

const emptyToNull = (prop) => {
  return prop === '' ? null : prop;
};
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: auto;
}
.table th,
.table td {
  vertical-align: middle; /* 수직 중앙 정렬 */
}
.table-hover tbody tr:hover {
  background-color: #f1f1f1; /* 마우스 오버 시 배경색 변경 */
}
.spinner-border {
  width: 2rem;
  height: 2rem;
}
.btn-danger {
  background-color: #dc3545;
  color: white;
}
.btn-danger:hover {
  background-color: #c82333;
}
.dropdown-menu.show {
  display: block;
  position: absolute;
  will-change: transform;
}
</style>
