import axiosInstance from '@/axiosInstance'; // axiosInstance 가져오기

const BASE_URL = '/api/like/report';

export default {
  async getList(params) {
    try {
      const { data } = await axiosInstance.get(BASE_URL, {
        params,
      });
      return data;
    } catch (error) {
      throw error;
    }
  },

  async create(reportNo) {
    try {
      const { data } = await axiosInstance.get(`${BASE_URL}/${reportNo}`);
      return data;
    } catch (error) {
      throw error;
    }
  },

  async delete(memberReportNo) {
    try {
      const { data } = await axiosInstance.delete(
        `${BASE_URL}/${memberReportNo}`
      );
      return data;
    } catch (error) {
      throw error;
    }
  },
};
