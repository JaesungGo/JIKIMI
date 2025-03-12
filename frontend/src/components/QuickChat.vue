<template>
  <div class="quick-chat">
    <button @click="toggleChat" class="chat-bot-button">
      <img src="@/assets/boogiChat.png" alt="Turtle Icon" class="turtle-icon" />
    </button>

    <div v-if="chatVisible" class="chat-box">
      <div class="header">
        <h3 class="title">나만의 부동산 도우미, 부기봇</h3>
        <button @click="toggleChat" class="close-button">
          <span>×</span>
        </button>
      </div>
      <div class="chat-container">
        <div class="messages" ref="messagesContainer">
          <div class="disclaimer">
            챗봇 서비스 사용 시 입력한 개인정보는 수집될 수 있습니다. 또한
            부동산 투자에 대한 피해에 대한 책임은 지지 않습니다.
          </div>
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="[
              'message',
              message.role === '부린이' ? 'user-message' : 'bot-message',
            ]"
          >
            <span
              class="message-content"
              v-html="sanitizeAndStyleMessage(message.content)"
            ></span>
            <span class="message-time">{{ getCurrentTime() }}</span>
          </div>
        </div>
        <div class="input-area">
          <input
            v-model="userInput"
            @keyup.enter="sendMessage"
            placeholder="메시지를 입력하세요"
            ref="inputField"
            class="message-input"
          />
          <button @click="sendMessage" class="send-button">전송</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axiosInstance from '../axiosInstance';
import DOMPurify from 'dompurify';

const chatVisible = ref(false);
const messages = ref([]);
const userInput = ref('');
const messagesContainer = ref(null);

// 현재 시간을 한국어 형식으로 반환하는 함수
const getCurrentTime = () => {
  const now = new Date();
  const hours = now.getHours();
  const minutes = now.getMinutes();
  const ampm = hours >= 12 ? '오후' : '오전';
  const formattedHours = hours % 12 || 12;
  const formattedMinutes = minutes.toString().padStart(2, '0');
  return `${ampm} ${formattedHours}:${formattedMinutes}`;
};

// HTML 태그를 안전하게 처리하고 스타일을 적용하는 함수
const sanitizeAndStyleMessage = (content) => {
  const config = {
    ALLOWED_TAGS: ['a', 'b', 'i', 'em', 'strong', 'span', 'div', 'p', 'br'],
    ALLOWED_ATTR: ['href', 'class', 'target'],
  };

  let sanitizedContent = DOMPurify.sanitize(content, config);

  sanitizedContent = sanitizedContent.replace(
    /<a([^>]*?)class=["']dictionary-link["']([^>]*?)>/g,
    '<a$1class="dictionary-link styled-link"$2>'
  );

  return sanitizedContent;
};

const toggleChat = () => {
  chatVisible.value = !chatVisible.value;
};

const loadMessages = () => {
  const storedMessages = localStorage.getItem('chatMessages');
  if (storedMessages) {
    messages.value = JSON.parse(storedMessages);
  }
};

const saveMessages = () => {
  localStorage.setItem('chatMessages', JSON.stringify(messages.value));
};

const scrollToBottom = () => {
  const container = messagesContainer.value;
  if (container) {
    container.scrollTop = container.scrollHeight;
  }
};

const sendMessage = async () => {
  if (userInput.value.trim() === '') return;

  const escapedInput = userInput.value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');

  messages.value.push({
    role: '부린이',
    content: escapedInput,
    time: getCurrentTime(),
  });
  saveMessages();

  try {
    const response = await axiosInstance.post('/chat/chatbot', {
      prompt: userInput.value,
    });

    const content = response.data.content || '';
    messages.value.push({
      role: '부기봇',
      content,
      time: getCurrentTime(),
    });
    saveMessages();
  } catch (error) {
    console.error('Error:', error);
    messages.value.push({
      role: '부기봇',
      content: '메시지 전송에 실패했습니다. 다시 시도해 주세요.',
      time: getCurrentTime(),
    });
    saveMessages();
  }

  userInput.value = '';
  scrollToBottom();
};

onMounted(() => {
  loadMessages();
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700&display=swap');

h3 {
    color: white;
}

.quick-chat {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  font-family: 'Noto Sans KR', sans-serif;
}

.chat-bot-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1173D6 0%, #0051d3 100%);
  box-shadow: 0 8px 16px rgba(0, 98, 255, 0.2);
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.chat-bot-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 20px rgba(0, 98, 255, 0.25);
}

.turtle-icon {
  width: 75px;
  height: 75px;
}

.chat-box {
  position: absolute;
  bottom: 100px;
  right: 0;
  width: 380px;
  height: 600px;
  background: #ffffff;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
  border-radius: 20px;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #1173D6;
  color: white;
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.close-button {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  font-size: 20px;
  transition: background-color 0.3s ease;
}

.close-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100% - 72px);
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f8f9ff;
}

.message {
  display: flex;
  flex-direction: column;
  max-width: 80%;
  margin-bottom: 16px;
  position: relative;
  animation: fadeIn 0.3s ease;
}

.user-message {
  align-self: flex-end;
  background: #1173D6;
  color: white;
  border-radius: 18px 18px 4px 18px;
  padding: 12px 16px;
  margin-left: auto;
}

.bot-message {
  align-self: flex-start;
  background: white;
  color: #1a1a1a;
  border-radius: 18px 18px 18px 4px;
  padding: 12px 16px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.message-content {
  margin-bottom: 4px;
  line-height: 1.5;
  font-size: 14px;
  letter-spacing: -0.3px;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
  text-align: right;
  margin-top: 4px;
}

.input-area {
  padding: 16px;
  background: white;
  border-top: 1px solid #eef0f5;
  display: flex;
  gap: 12px;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #eef0f5;
  border-radius: 25px;
  font-size: 14px;
  font-family: 'Noto Sans KR', sans-serif;
  transition: border-color 0.3s ease;
}

.message-input:focus {
  outline: none;
  border-color: #1173D6;
}

.send-button {
  background: #1173D6;
  color: white;
  border: none;
  border-radius: 25px;
  padding: 0 24px;
  font-weight: 500;
  font-family: 'Noto Sans KR', sans-serif;
  cursor: pointer;
  transition: all 0.3s ease;
}

.send-button:hover {
  background: #0051d3;
  transform: translateY(-1px);
}

.disclaimer {
  text-align: center;
  color: #8f9bb3;
  font-size: 12px;
  padding: 12px;
  margin-bottom: 16px;
  background: rgba(0, 98, 255, 0.05);
  border-radius: 8px;
  line-height: 1.5;
}

/* HTML 태그가 포함된 메시지를 위한 스타일 */
:deep(.styled-link) {
  color: #0062ff !important;
  font-weight: 500;
  text-decoration: none;
  position: relative;
  transition: all 0.3s ease;
}

:deep(.styled-link):hover {
  color: #0051d3 !important;
  text-decoration: underline;
}

:deep(.styled-link)::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 1px;
  background-color: currentColor;
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

:deep(.styled-link):hover::after {
  transform: scaleX(1);
}

.bot-message :deep(.styled-link) {
  color: #0062ff !important;
  font-weight: 500;
}

.bot-message :deep(.styled-link):hover {
  color: #0051d3 !important;
}

.user-message :deep(.styled-link) {
  color: white !important;
  font-weight: 500;
}

.user-message :deep(.styled-link):hover {
  color: rgba(255, 255, 255, 0.9) !important;
}

:deep(p) {
  margin: 0;
  line-height: 1.5;
}

:deep(br) {
  content: '';
  display: block;
  margin: 8px 0;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.messages::-webkit-scrollbar {
  width: 6px;
}

.messages::-webkit-scrollbar-track {
  background: #f8f9ff;
}

.messages::-webkit-scrollbar-thumb {
  background: #0062ff40;
  border-radius: 3px;
}

.messages::-webkit-scrollbar-thumb:hover {
  background: #0062ff60;
}
</style>
