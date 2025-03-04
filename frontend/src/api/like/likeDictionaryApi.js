// src/api/dictionaryApi.js
import axiosInstance from '@/axiosInstance'; // axiosInstance 가져오기

const BASE_URL = '/api/like/dictionary';

export default {
  async getList() {
    try {
      const { data } = await axiosInstance.get(BASE_URL);
      return data;
    } catch (error) {
      throw error;
    }
  },

  async create(dicNo) {
    try {
      const { data } = await axiosInstance.get(`${BASE_URL}/${dicNo}`);
      return data;
    } catch (error) {
      throw error;
    }
  },

  async delete(dicNo) {
    try {
      const { data } = await axiosInstance.delete(`${BASE_URL}/${dicNo}`);
      return data;
    } catch (error) {
      throw error;
    }
  },
};
