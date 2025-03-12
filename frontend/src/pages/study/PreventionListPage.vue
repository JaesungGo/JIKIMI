<template>
  <div class="min-h-screen bg-gradient-to-br from-[#F0F7FF] to-[#F0F7FF] py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-3xl mx-auto">
      <h1 class="text-4xl font-bold text-center mb-8" style="color: #1173D6;">
        <i class="fas fa-search mr-2"></i> 계약 전, 꼭 확인해보세요.
      </h1>

      <div v-if="isLoading" class="flex justify-center items-center py-12">
        <div class="animate-spin rounded-full h-16 w-16 border-t-4 border-indigo-600"></div>
      </div>

      <div v-else-if="errorMessage" class="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-md mb-6" role="alert">
        <p class="font-bold">오류</p>
        <p>{{ errorMessage }}</p>
      </div>

      <div v-else class="space-y-6">
        <article v-for="article in articles" :key="article.link" 
                 class="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-2xl transition duration-300 transform hover:-translate-y-1">
          <a :href="article.link" target="_blank" rel="noopener noreferrer" class="block p-6">
            <h2 class="text-2xl font-bold text-gray-900 mb-2 line-clamp-2">{{ article.title }}</h2>
            <p class="text-gray-600 mb-4 line-clamp-3">{{ article.description }}</p>
            <div class="flex justify-between items-center text-sm text-gray-500">
              <span class="flex items-center">
                <i class="far fa-calendar-alt mr-2"></i>
                {{ formatDate(article.postdate) }}
              </span>
              <span class="flex items-center">
                <i class="far fa-user mr-2"></i>
                {{ article.bloggername }}
              </span>
            </div>
          </a>
        </article>
      </div>

      <div class="mt-10 flex justify-center">
        <nav class="flex items-center bg-white px-4 py-3 rounded-lg shadow-md">
          <button @click="handlePageChange(currentPage - 1)" 
                  :disabled="currentPage === 1"
                  class="mr-2 px-4 py-2 text-sm font-medium text-[#1173D6] bg-[#D6E8FA] rounded-md hover:bg-[#B0D5F6] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#1173D6] disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200">
            <i class="fas fa-chevron-left mr-1"></i> 이전
          </button>
          <span class="px-4 py-2 rounded-md text-sm font-semibold bg-[#1173D6] text-white">
            {{ currentPage }}
          </span>
          <button @click="handlePageChange(currentPage + 1)"
                  :disabled="!hasMorePages"
                  class="ml-2 px-4 py-2 text-sm font-medium text-[#1173D6] bg-[#D6E8FA] rounded-md hover:bg-[#B0D5F6] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[#1173D6] disabled:opacity-50 disabled:cursor-not-allowed transition-colors duration-200">
            다음 <i class="fas fa-chevron-right ml-1"></i>
          </button>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axiosInstance from '../../axiosInstance';

const articles = ref([]);
const isLoading = ref(true);
const errorMessage = ref('');
const currentPage = ref(1);
const hasMorePages = ref(true);
const itemsPerPage = 10;

const decodeHtmlEntities = (text) => {
  const textArea = document.createElement('textarea');
  textArea.innerHTML = text;
  return textArea.value;
};

const stripHtmlTags = (text) => {
  return text.replace(/<[^>]*>/g, '');
};

const fetchBlogs = async (page) => {
  isLoading.value = true;
  errorMessage.value = '';
  try {
    const response = await axiosInstance.get('/blog', {
      params: {
        query: '전세사기',
        display: itemsPerPage,
        start: (page - 1) * itemsPerPage + 1
      }
    });

    articles.value = response.data.items.map(item => ({
      title: decodeHtmlEntities(stripHtmlTags(item.title)),
      link: item.link,
      description: decodeHtmlEntities(stripHtmlTags(item.description)),
      bloggername: item.bloggername,
      bloggerlink: item.bloggerlink,
      postdate: item.postdate 
    }));

    hasMorePages.value = response.data.total > page * itemsPerPage;
  } catch (error) {
    console.error('블로그를 불러오는데 실패했습니다:', error);
    errorMessage.value = '블로그를 불러오는데 실패했습니다. 다시 시도해 주세요.';
  } finally {
    isLoading.value = false;
  }
};

const handlePageChange = (newPage) => {
  if (newPage < 1 || (newPage > currentPage.value && !hasMorePages.value)) return;
  currentPage.value = newPage;
  fetchBlogs(newPage);
};

const formatDate = (dateStr) => {
  if (!dateStr || dateStr.length < 8) return ''; 
  return `${dateStr.slice(0, 4)}.${dateStr.slice(4, 6)}.${dateStr.slice(6)}`;
};

onMounted(() => {
  fetchBlogs(1);
});
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700&display=swap');

body {
  font-family: 'Noto Sans KR', sans-serif;
}
</style>
