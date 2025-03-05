<template>
  <div>
    <div>{{ provider }} 리디렉션 중...</div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import axiosInstance  from '@/axiosInstance';
import { useRouter, useRoute } from 'vue-router';

export default {
  name: 'OAuthRedirectPage',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const code = ref('');
    const provider = ref('');

    const handleOAuth = async (code, provider) => {
      try {
        const response = await axiosInstance.get(`/oauth/login/${provider}?code=${code}`,
         {withCredentials: true});
        router.push("/");
      } catch (error) {
        console.error(`${provider} 로그인 실패:`, error);
        router.push('/login');
      }
    };

    onMounted(() => {
      const searchParams = new URLSearchParams(window.location.search);
      code.value = searchParams.get('code');
      provider.value = route.params.provider;

      if (code.value && provider.value) {
        handleOAuth(code.value, provider.value);
      }
    });

    return { code, provider };
  },
};
</script>

<style scoped>
</style>
