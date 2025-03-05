import axiosInstance from '@/axiosInstance'; // axiosInstance 가져오기

const BASE_URL = '/chat/scenario';

export default {
  // 주소 리스트를 가져오는 함수
  async getScenarioData(selectedAnswers) {
    try {
      const { data } = await axiosInstance.post(
        BASE_URL,{selectedAnswers},
        {
          headers: { 'Content-Type': 'application/json' },
        }
      );
      return data;
    } catch (error) {
      throw error; // 에러 전파
    }
  },
};
