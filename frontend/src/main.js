import './assets/main.css';
import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
// import './assets/style.css'; // 커스텀 CSS 불러오기
import 'vue-awesome-paginate/dist/style.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import './assets/tailwind.css'; // Tailwind CSS 추가
import { library } from '@fortawesome/fontawesome-svg-core';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { faCircleQuestion } from '@fortawesome/free-regular-svg-icons';
import { faCircleQuestion as faSolidCircleQuestion } from '@fortawesome/free-solid-svg-icons';
import VueAwesomePaginate from 'vue-awesome-paginate';
import { fas } from '@fortawesome/free-solid-svg-icons';
import { far } from '@fortawesome/free-regular-svg-icons';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
// 필요한 아이콘을 라이브러리에 추가
library.add(fas, far);

const app = createApp(App);

// 아이콘 컴포넌트 등록
app.component('font-awesome-icon', FontAwesomeIcon);

const pinia = createPinia();
app.use(VueAwesomePaginate);
app.use(pinia); // 이미 생성한 pinia 사용
app.use(router);

app.mount('#app');
app.use(ElementPlus);
