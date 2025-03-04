<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="login-title">로그인</h1>
      <p class="login-description">
        SNS로 간편하게 로그인하고 더 많은 서비스를 즐겨보세요!
      </p>

      <!-- 네이버 로그인 버튼 -->
      <button class="login-button naver" @click="handleNaverLogin">
        <img src="@/assets/naver.png" alt="Naver" class="button-icon" />
        네이버로 계속하기
      </button>

      <!-- 구글 로그인 버튼 -->
      <button class="login-button google" @click="handleGoogleLogin">
        <img src="@/assets/google.png" alt="Google" class="button-icon" />
        Google로 계속하기
      </button>
    </div>
  </div>
</template>

<script>
import axiosInstance from '@/axiosInstance';
import { data } from 'autoprefixer';

export default {
  name: 'login',
  methods: {
    
    handleNaverLogin() {
      axiosInstance.get('/oauth/NAVER')
      return data;
    },
    handleGoogleLogin() {
      axiosInstance.get('/oauth/NAVER')
      return data;
    },
    async fetchToken(code, provider) {
      try {
        // 인증 서버에서 JWT 쿠키를 설정
        const response = await axiosInstance.get(
          `/oauth/login/${provider}?code=${code}`,
          {
            withCredentials: true, // 쿠키를 클라이언트에 저장
          }
        );
        return response.data;
      } catch (error) {
        console.error('Login failed', error);
        throw error;
      }
    },
    async handleRedirect() {
      const urlParams = new URLSearchParams(window.location.search);
      const code = urlParams.get('code');
      const provider = urlParams.get('provider');
      if (code && provider) {
        await this.fetchToken(code, provider);
        this.$router.push('/');
      }
    },
  },
  mounted() {
    this.handleRedirect();
  },
};
</script>

<style scoped>
/* 페이지 중앙 배치 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
  background-color: #F0F7FF;
}

/* 로그인 박스 크기 */
.login-box {
  background-color: #ffffff;
  padding: 50px; /* 여백을 넉넉하게 추가 */
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
  width: 600px;
  min-height: 400px; /* 최소 높이를 크게 설정 */
}

/* 로그인 제목 크기 */
.login-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
}

/* 설명 문구 */
.login-description {
  font-size: 16px;
  color: #6c757d;
  margin-bottom: 30px;
}

/* 빠른 회원가입 버튼 */
.quick-join {
  background-color: #4f50ec;
  color: white;
  padding: 15px 0; /* 높이를 더 키움 */
  border: none;
  border-radius: 5px;
  margin-bottom: 30px;
  font-size: 16px;
  width: 100%;
  cursor: pointer;
}

/* 로그인 버튼 크기 */
.login-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 18px; /* 버튼 높이를 더 키움 */
  margin-bottom: 15px; /* 버튼 사이 간격을 넓힘 */
  border: none;
  border-radius: 8px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
}

/* 카카오 버튼 */
.kakao {
  background-color: #fee500;
  color: #3c1e1e;
}

/* 네이버 버튼 */
.naver {
  background-color: #03c75a;
  color: white;
}

/* 구글 버튼 */
.google {
  background-color: white;
  color: #555;
  border: 1px solid #ddd;
}

/* 버튼 아이콘 크기 */
.button-icon {
  width: 24px;
  height: 24px;
  margin-right: 10px;
}

@media (max-width: 600px) {
  .login-box {
    width: 90%;
  }
}

</style>