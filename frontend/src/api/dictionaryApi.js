// src/api/dictionaryApi.js
import axiosInstance from '@/axiosInstance'; // axiosInstance 가져오기

const BASE_URL = '/api/dictionary';

export default {
  async getList() {
    try {
      const { data } = await axiosInstance.get(BASE_URL);
      return data;
    } catch (error) {
      throw error;
    }
  },

  async get(no) {
    try {
      const { data } = await axiosInstance.get(`${BASE_URL}/${no}`);
      return data;
    } catch (error) {
      throw error;
    }
  },
};
