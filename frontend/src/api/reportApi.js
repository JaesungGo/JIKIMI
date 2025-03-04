import axiosInstance from '@/axiosInstance'; // axiosInstance 가져오기

const BASE_URL = '/api/report';

export default {
  // 주소 리스트를 가져오는 함수
  async getReportData(analysisNo) {
    try {
      const { data } = await axiosInstance.get(
        `${BASE_URL}/${analysisNo}`,
        {
          headers: { 'Content-Type': 'application/json' },
        }
      );
      return data;
    } catch (error) {
      throw error; // 에러 전파
    }
  },
  // 선택된 항목을 삭제하는 함수
  async deleteReportData(analysisNo) {
    try {
      const { data } = await axiosInstance.delete(
        `${BASE_URL}/${analysisNo}`,
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
