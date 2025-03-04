import axiosInstance from '@/axiosInstance'; // axiosInstance 가져오기

const BASE_URL = '/api/prevention';

export default {
  async getList(params) {
    try {
      const { data } = await axiosInstance.get(BASE_URL, { params });
      return data;
    } catch (error) {
      throw error; // 에러 전파
    }
  },

  async get(no) {
    try {
      const { data } = await axiosInstance.get(`${BASE_URL}/${no}`);
      return data;
    } catch (error) {
      throw error; // 에러 전파
    }
  },
};
