<template>
  <div class="container">
    <div class="checklist-section" v-show="!showResults">
      <h1 class="title">전세 사기 위험 체크리스트</h1>

      <form @submit.prevent="submitChecklist" class="checklist-form">
        <div
          v-for="(question, index) in questions"
          :key="index"
          class="question-item"
        >
          <p class="question-text">{{ question.text }}</p>
          <div class="radio-group">
            <label
              v-for="option in ['예', '아니오', '모르겠습니다']"
              :key="option"
              class="radio-label"
              :class="{ 'checked-label': selectedAnswers[index] === getOptionValue(option) }"
            >
              <input
                type="radio"
                :name="'question-' + index"
                :value="getOptionValue(option)"
                v-model="selectedAnswers[index]"
              />
              <span class="radio-custom"></span>
              <span class="radio-text">{{ option }}</span>
            </label>
          </div>
        </div>
        <button type="submit" :disabled="!allQuestionsAnswered || loading" class="submit-button">
          <span v-if="!loading">사기 시나리오 확인하기</span>
          <span v-else class="loading-spinner"></span>
        </button>
      </form>
    </div>

    <div class="results-section" v-show="showResults">
      <h1 class="title">시나리오가 도착했어요!</h1>
      <Loading v-if="loading" />

      <div v-else>
        <div v-if="scenarios.length === 0" class="no-results">
          <p>시나리오를 찾을 수 없습니다.</p>
        </div>
        <div v-else class="scenarios-container">
          <div
            v-for="(scenario, index) in scenarios"
            :key="index"
            class="scenario-card"
            :class="{ 'active': selectedScenario === index }"
          >
            <div class="scenario-main">
              <div class="scenario-header">
                <div class="scenario-title">
                  <span class="scenario-number">CASE {{ index + 1 }}</span>
                  <h2>{{ scenario.name }}</h2>
                </div>
                <div class="fraud-badge">
                  <span class="fraud-type">{{ scenario.type }}</span>
                </div>
              </div>
              <div class="scenario-content">
                <p class="fraud-scenario">{{ scenario.fraud }}</p>
                <button 
                  class="toggle-solution"
                  @click="toggleSolution(index)"
                >
                  <span class="toggle-text">대처 방안 {{ selectedScenario === index ? '접기' : '펼치기' }}</span>
                  <span class="toggle-icon" :class="{ 'rotated': selectedScenario === index }">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </span>
                </button>
              </div>
            </div>
            <div 
              class="solution-panel"
              :class="{ 'show': selectedScenario === index }"
            >
              <div class="solution-content">
                <div class="solution-header">
                  <span class="solution-icon">🛡️</span>
                  <h3>대처 방안</h3>
                </div>
                <p>{{ scenario.address }}</p>
              </div>
            </div>
          </div>
        </div>
        <button @click="resetForm" class="reset-button">다시 체크하기</button>
      </div>
    </div>
  </div>
</template>

<script>
import Loading from '@/pages/Loading.vue';
import scenarioApi from '@/api/scenarioApi';

export default {
  components: {
    Loading,
  },
  data() {
    return {
      questions: [
        {
          text: '집 주인과 계약하려는 사람이 같은 사람인가요?',
          message: 'A situation where the person trying to contract is different from the actual owner of the house',
        },
        {
          text: '집 주인이 보증금을 줄여주겠다고 하나요?',
          message: 'A situation where the owner offers a deposit much lower than the market price',
        },
        {
          text: '집 주인이 집의 소유증명서를 보여주지 않나요?',
          message: 'A situation where the owner does not show or refuses to provide the proof of ownership',
        },
        {
          text: '이 집에 다른 세입자가 살고 있나요?',
          message: 'A situation where there are already other tenants living in the house I am trying to contract with',
        },
        {
          text: '집의 상태나 위치가 설명과 많이 다른가요?',
          message: 'A situation where the information in the advertisement and the actual condition or location of the property is significantly different',
        },
        {
          text: '집 주인이 급하게 계약하자고 하나요?',
          message: 'A situation where the owner is trying to rush the contract and pressuring me to make a quick decision',
        },
        {
          text: '계약을 갑자기 미루자고 하나요?',
          message: 'A situation where the landlord suddenly tries to postpone the contract and may later become unreachable or vanish',
        },
        {
          text: '계약서를 대충 작성하자고 하나요?',
          message: 'A situation where the contract is poorly written and important details are missing',
        },
        {
          text: '보증금을 돌려주는 보험 가입을 거부하나요?',
          message: 'A situation where the owner refuses to sign up for insurance that returns the deposit',
        },
        {
          text: '임대인이 세입자의 신분증이나 서류를 지나치게 요구하나요?',
          message: 'A situation where the landlord excessively demands personal information like ID or other documents',
        },
      ],
      selectedAnswers: Array(10).fill(null),
      scenarios: [],
      loading: false,
      showResults: false,
      selectedScenario: null,
    };
  },
  computed: {
    allQuestionsAnswered() {
      return this.selectedAnswers.every(answer => answer !== null);
    },
  },
  methods: {
    getOptionValue(option) {
      return option === '예' ? 'yes' : option === '아니오' ? 'no' : 'unknown';
    },
    async submitChecklist() {
      this.loading = true;
      this.showResults = true;

      const selectedMessages = this.questions
        .filter((question, index) => this.selectedAnswers[index] === 'yes')
        .map((question) => question.message);

      try {
        const data = await scenarioApi.getScenarioData(selectedMessages);
        this.scenarios = this.extractScenarios(data.content);
      } catch (error) {
        console.error('문제가 발생했습니다:', error);
      } finally {
        this.loading = false;
      }
    },
    toggleSolution(index) {
      this.selectedScenario = this.selectedScenario === index ? null : index;
    },
    extractScenarios(content) {
      const nameRegex = /\$start-name\$(.*?)\$end-name\$/gs;
      const fraudRegex = /\$start-fraud\$(.*?)\$end-fraud\$/gs;
      const typeRegex = /\$start-type\$(.*?)\$end-type\$/gs;
      const addressRegex = /\$start-address\$(.*?)\$end-address\$/gs;

      const names = [...content.matchAll(nameRegex)].map(match => match[1].trim());
      const frauds = [...content.matchAll(fraudRegex)].map(match => match[1].trim());
      const types = [...content.matchAll(typeRegex)].map(match => match[1].trim());
      const addresses = [...content.matchAll(addressRegex)].map(match => match[1].trim());

      const scenarios = names.map((name, index) => ({
        name,
        fraud: frauds[index] || '',
        type: types[index] || '',
        address: addresses[index] || ''
      }));

      return scenarios;
    },
    resetForm() {
      this.selectedAnswers = Array(10).fill(null);
      this.scenarios = [];
      this.showResults = false;
      this.selectedScenario = null;
    },
  },
};
</script>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 60px 20px;
  font-family: 'Pretendard', 'Noto Sans KR', sans-serif;
  background-color: #f0f7ff;
}

.title {
  background: linear-gradient(135deg, #0050b3 0%, #1890ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-size: 3.5rem;
  text-align: center;
  margin-bottom: 4rem;
  font-weight: 700;
  letter-spacing: -0.03em;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.checklist-form {
  background-color: white;
  padding: 3.5rem;
  border-radius: 30px;
  box-shadow: 0 10px 40px rgba(24, 144, 255, 0.1);
}

.question-item {
  margin-bottom: 2.5rem;
  padding: 2.5rem;
  border: 2px solid #e6f7ff;
  border-radius: 20px;
  transition: all 0.3s ease;
  background: #ffffff;
}

.question-item:hover {
  box-shadow: 0 8px 16px rgba(24, 144, 255, 0.1);
  transform: translateY(-4px);
}

.question-text {
  font-size: 1.3rem;
  color: #003a8c;
  margin-bottom: 1.5rem;
  font-weight: 700;
  line-height: 1.6;
  letter-spacing: -0.01em;
  text-align: left;
}

.radio-group {
  display: flex;
  justify-content: space-between;
  gap: 1.5rem;
}

.radio-label {
  flex: 1;
  position: relative;
  padding: 1.25rem;
  background: #f0f7ff;
  border: 2px solid #bae7ff;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.radio-label.checked-label {
  background: #1890ff; /* 체크된 상태의 배경색 */
  border-color: #0050b3; /* 체크된 상태의 테두리 색상 */
  color: white; /* 체크된 상태의 글자 색상 */
}

.radio-group input[type='radio'] {
  position: absolute;
  opacity: 0;
}

.radio-text {
  font-size: 1.1rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.radio-group input[type='radio']:checked + .radio-text {
  color: white;
  font-weight: bold;
}

.radio-group input[type='radio']:checked + .radio-text::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #1890ff;
  border-color: #1890ff;
  border-radius: 13px;
  z-index: -1;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.submit-button,
.reset-button {
  width: 100%;
  padding: 1.25rem;
  background: linear-gradient(135deg, #0050b3 0%, #1890ff 100%);
  color: white;
  border: none;
  border-radius: 15px;
  font-size: 1.3rem;
  font-weight: 700;
  cursor: pointer;
  margin-top: 2rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.submit-button:hover,
.reset-button:hover {
  background: linear-gradient(135deg, #003a8c 0%, #0050b3 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
}

.submit-button:disabled {
  background: #bae7ff;
  cursor: not-allowed;
  box-shadow: none;
}

.loading-spinner {
  display: inline-block;
  width: 28px;
  height: 28px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.results-section {
  background-color: white;
  padding: 3.5rem;
  border-radius: 30px;
  box-shadow: 0 10px 40px rgba(24, 144, 255, 0.1);
}

.scenario-card {
  margin-bottom: 2.5rem;
  border: 2px solid #e6f7ff;
  border-radius: 20px;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.scenario-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 32px rgba(24, 144, 255, 0.15);
}

.scenario-main {
  padding: 2.5rem;
  overflow: hidden;
}

.scenario-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.scenario-title {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  flex: 1;
  margin-right: 1rem;
}

.scenario-number {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1890ff;
  letter-spacing: 0.05em;
  background: rgba(24, 144, 255, 0.1);
  padding: 0.6rem 1.2rem;
  border-radius: 25px;
  display: inline-block;
}

.scenario-title h2 {
  font-size: 1.5rem;
  color: #003a8c;
  margin: 0;
  font-weight: 800;
  line-height: 1.3;
  letter-spacing: -0.02em;
  word-break: keep-all;
  overflow-wrap: break-word;
}

.fraud-badge {
  align-self: flex-start;
  margin-top: 0.5rem;
  background: rgba(24, 144, 255, 0.1);
  color: #0050b3;
  padding: 0.5rem 1.5rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.fraud-type {
  color: #0078d4;
  padding: 0.5rem 2rem;
  border-radius: 24px;
  font-size: 1.1rem;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.scenario-content {
  color: #262626;
  padding: 1.5rem;
  background: #f0f7ff;
  border-radius: 20px;
}

.fraud-scenario {
  font-size: 1.2rem;
  line-height: 1.8;
  margin-bottom: 2.5rem;
  font-weight: 500;
  letter-spacing: -0.01em;
  white-space: pre-line;
  color: #262626;
}

.toggle-solution {
  width: 100%;
  padding: 1.5rem;
  background: rgba(24, 144, 255, 0.1);
  border: 2px solid rgba(24, 144, 255, 0.3);
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1.2rem;
  color: #0050b3;
  font-weight: 600;
  transition: all 0.3s ease;
}

.toggle-solution:hover {
  background: rgba(24, 144, 255, 0.2);
}

.toggle-icon {
  transition: transform 0.3s ease;
}

.toggle-icon.rotated {
  transform: rotate(180deg);
}

.solution-panel {
  max-height: 0;
  overflow: hidden;
  transition: all 0.5s ease-out;
  background: #f9fcff;
}

.solution-panel.show {
  max-height: 2000px;
}

.solution-content {
  padding: 3rem;
  border-top: 2px solid #e6f7ff;
}

.solution-header {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  margin-bottom: 2rem;
  padding-bottom: 1.25rem;
  border-bottom: 3px solid rgba(24, 144, 255, 0.2);
}

.solution-icon {
  font-size: 2rem;
}

.solution-header h3 {
  color: #003a8c;
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.02em;
}

.solution-content p {
  color: #262626;
  font-size: 1.2rem;
  line-height: 1.8;
  font-weight: 500;
  letter-spacing: -0.01em;
  white-space: pre-line;
}

@media (max-width: 768px) {
  .container {
    padding: 40px 15px;
  }

  .title {
    font-size: 2.5rem;
  }

  .checklist-form,
  .results-section {
    padding: 2rem;
  }

  .question-item,
  .scenario-main,
  .solution-content {
    padding: 1.5rem;
  }

  .radio-group {
    flex-direction: column;
    gap: 1rem;
  }

  .radio-label {
    width: 100%;
  }

  .scenario-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .scenario-title {
    margin-right: 0;
    margin-bottom: 1rem;
  }

  .scenario-title h2 {
    font-size: 1.6rem;
  }

  .fraud-badge {
    align-self: flex-start;
  }

  .fraud-type {
    font-size: 0.9rem;
    padding: 0.6rem 1.2rem;
  }

  .fraud-scenario,
  .solution-content p {
    font-size: 1.1rem;
  }
}
</style>
