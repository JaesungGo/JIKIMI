import axiosInstance from '@/axiosInstance'; // axiosInstance 가져오기

const BASE_URL = '/api/test'; // Your base URL

export default {
  async createCor(params) {
    try {
      const { data } = await axiosInstance.get(`${BASE_URL}/create/cor`, {
        params,
      });
      return data;
    } catch (error) {
      throw error;
    }
  },
  async createBml(params) {
    try {
      const { data } = await axiosInstance.get(`${BASE_URL}/create/bml`, {
        params,
      });
      return data;
    } catch (error) {
      throw error;
    }
  },
  async analysis(params) {
    try {
      const { data } = await axiosInstance.get(`${BASE_URL}/analysis`, {
        params,
      });
      return data;
    } catch (error) {
      throw error;
    }
  },
};
