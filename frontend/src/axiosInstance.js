// src/axiosInstance.js
import axios from 'axios';
import Cookies from 'js-cookie';

const axiosInstance = axios.create({
  baseURL: '/api', // 기본 URL
  withCredentials: true, // 쿠키 전송을 허용
  headers: {
    'Content-Type': 'application/json', // 기본 Content-Type
  },
});

// 요청 인터셉터를 추가하여 필요한 작업을 수행할 수 있습니다.
axiosInstance.interceptors.request.use(config => {
    const token = Cookies.get('jwtToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default axiosInstance;