import axiosInstance from '@/axiosInstance'; // axiosInstance 가져오기

const BASE_URL = '/oauth/info'; // Your base URL

export default {
  // GET request to retrieve user info
  //URL이 있는 param 주로 사용
  async getInfo() {
    try {
      const { data } = await axiosInstance.get(BASE_URL); // Fetch user info
      return data;
    } catch (error) {
      throw error; // Propagate the error
    }
  },

  // PUT request to update user info -홍관님 여쭤보기
  async updateInfo(userInfo) {
    try {
      const { data } = await axiosInstance.put(BASE_URL, userInfo); // Update user info
      return data;
    } catch (error) {
      throw error; // Propagate the error
    }
  },
};
