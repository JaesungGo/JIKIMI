import axiosInstance from '@/axiosInstance'; // axiosInstance 가져오기

const BASE_URL = '/api/map';

export default {
  // 주소 리스트를 가져오는 함수
  async getAddressList() {
    try {
      const { data } = await axiosInstance.get(BASE_URL, {
        headers: { 'Content-Type': 'application/json' },
      });
      return data;
    } catch (error) {
      throw error; // 에러 전파
    }
  },

  // 지도 이동 시 중심 좌표와 줌 레벨을 기준으로 주소 리스트를 가져오는 함수
  async getAddressListMoveAll(lat, lon, zoomLevel) {
    try {
      // console.log('API call with params:', {
      //   lat,
      //   lon,
      //   zoomLevel,
      // });

      const { data } = await axiosInstance.get(
        `${BASE_URL}/moveall`,
        {
          headers: { 'Content-Type': 'application/json' },
          params: { lat, lon, zoomLevel }, // 위도, 경도, 줌 레벨을 파라미터로 전달
        }
      );
      return data;
    } catch (error) {
      throw error; // 에러 전파
    }
  },

  async getAddressListMoveClusterAll(
    lat,
    lon,
    zoomLevel,
    page = 1,
    limit = 200
  ) {
    try {
      const { data } = await axiosInstance.get(
        `${BASE_URL}/moveclusterall`,
        {
          headers: { 'Content-Type': 'application/json' },
          params: { lat, lon, zoomLevel, page, limit }, // 위도, 경도, 줌 레벨을 파라미터로 전달
        }
      );
      return data;
    } catch (error) {
      throw error; // 에러 전파
    }
  },

  async getAddressDetails(id) {
    try {
      const { data } = await axiosInstance.get(
        `${BASE_URL}/${id}`
      );
      // console.log(data);
      return data;
    } catch (error) {
      throw error;
    }
  },
  async getReportAddressDetails(id) {
    try {
      const { data } = await axiosInstance.get(
        `${BASE_URL}/report/${id}`
      );
      // console.log(data);
      return data;
    } catch (error) {
      throw error;
    }
  },
};
