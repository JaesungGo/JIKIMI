// src/stores/userStore.js
import { defineStore } from 'pinia';
import axiosInstance from '../axiosInstance';

export const useUserStore = defineStore('user', {
  state: () => ({
    userName: '',
    userEmail: '',
  }),
  actions: {
    async fetchUserData() {
      try {
        // 서버에서 사용자 정보 가져오는 API 요청
        const response = await axiosInstance.get('/user'); // 여기에 실제 API 경로 입력
        const userData = response.data;

        // 스토어 상태 업데이트
        this.userName = userData.member_name;
        this.userEmail = userData.member_email;
      } catch (error) {
        console.error('사용자 정보를 불러오는 데 실패했습니다:', error);
      }
    },
    logout() {
      // 로그아웃 처리 (예시)
      this.userName = '';
      this.userEmail = '';
    },
  },
});
