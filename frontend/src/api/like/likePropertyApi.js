// src/api/dictionaryApi.js
import axiosInstance from '@/axiosInstance'; // axiosInstance 가져오기

const BASE_URL = '/api/like/property';

export default {
  async create(proNo) {
    try {
      const { data } = await axiosInstance.get(`${BASE_URL}/${proNo}`);
      return data;
    } catch (error) {
      throw error;
    }
  },

  async getList() {
    try {
      const { data } = await axiosInstance.get(BASE_URL);
      return data;
    } catch (error) {
      throw error;
    }
  },

  async delete(proNo) {
    try {
      const { data } = await axiosInstance.delete(`${BASE_URL}/${proNo}`);
      return data;
    } catch (error) {
      throw error;
    }
  },
};
