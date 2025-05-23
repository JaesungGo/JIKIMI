import { createRouter, createWebHistory } from 'vue-router';
import useAuthStore from '@/stores/auth';
import Main from '../pages/app/Main.vue';
import Login from '../pages/Login.vue';
import MyPage from '../pages/mypage/Mypage.vue';
import Analyzing from '@/pages/app/Analyzing.vue';
import FAQ from '@/pages/FAQ.vue';
import Loading from '@/pages/Loading.vue';
import ScenarioMain from '@/pages/scenario/ScenarioMain.vue';
// import ScenarioResult from '@/pages/scenario/ScenarioResult.vue';
import Introduce from '@/pages/app/Introduce.vue';
import OauthRedirectPage from '@/pages/OauthRedirectPage.vue';
import SenseDetailPage from '@/pages/study/commonsense/SenseDetailPage.vue';
import SenseListPage from '@/pages/study/commonsense/SenseListPage.vue';
import DictionaryListPage from '@/pages/study/dictionary/DictionaryListPage.vue';
import DictionaryDetailPage from '@/pages/study/dictionary/DictionaryDetailPage.vue';
import MainMap from '../pages/map/MainMap.vue';
import PreventionListPage from '@/pages/study/PreventionListPage.vue';
import PreventionDetailPage from '@/pages/study/PreventionDetailPage.vue';
import MapAnalyzing from '@/pages/map/MapAnalyzing.vue';
import MyReport from '@/pages/mypage/MyReport.vue';
import Report from '@/pages/app/Report.vue';
import Test from '@/pages/Test.vue';
import MyInfo from '@/pages/mypage/MyInfo.vue';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/mypage', 
      name: 'mypage', 
      component: MyPage,
      children: [
        {
        name: 'mypage',
        path: '',
        component: MyInfo,
        },
        {
          path: 'myreport',
          component: MyReport,
        },
        {
          path: 'myinfo',
          component: MyInfo, 
        },
      ], 
      meta: { requiresAuth: true },
    },

    // {
    //   path: '/myreport',
    //   name: 'myreport',
    //   component: MyReport,
    //   meta: { requiresAuth: true },
    // },
    {
      path: '/analyzing',
      name: 'analyzing',
      component: Analyzing,
      meta: { requiresAuth: true },
    },

    { 
      path: '/faq', 
      name: 'faq', 
      component: FAQ 
    },
    {
      path: '/introduce',
      name: 'introduce',
      component: Introduce,
    },
    { path: '/', name: 'main', component: Main },
    { path: '/login', name: 'login', component: Login },
    {
      path: '/scenario',
      name: 'ScenarioMain',
      component: ScenarioMain,
      meta: { requiresAuth: true },
    },
    // {
    //   path: '/scenario/result',
    //   name: 'ScenarioResult',
    //   component: ScenarioResult,
    // },
    {
      path: '/loading',
      name: 'Loading',
      component: Loading,
    },
    {
      path: '/oauth/redirected/:provider',
      name: 'OAuthRedirectPage',
      component: OauthRedirectPage,
    },
    {
      path: '/study/commonsense/detail/:no',
      name: 'senseDetailPage',
      component: SenseDetailPage,
    },
    {
      path: '/study/commonsense/list',
      name: 'senseListPage',
      component: SenseListPage,
    },

    {
      path: '/study/dictionary/list',
      name: 'dictionaryList',
      component: DictionaryListPage,
      meta: { requiresAuth: true },
    },
    {
      path: '/study/dictionary/detail/:no',
      name: 'dictionaryDetailPage',
      component: DictionaryDetailPage,
      meta: { requiresAuth: true },
    },
    {
      path: '/map',
      name: 'map',
      component: MainMap,
    },
    {
      path: '/study/prevention/list',
      name: 'preventionList',
      component: PreventionListPage,
    },
    {
      path: '/study/prevention/detail/:no',
      name: 'preventionDetail',
      component: PreventionDetailPage,
    },
    {
      path: '/map/analyzing',
      name: 'mapAnalyzing',
      component: MapAnalyzing,
      meta: { requiresAuth: true },
    },
    {
      path: '/report/:no',
      name: 'report',
      component: Report,
      meta: { requiresAuth: true },
    },
    {
      path: '/test',
      name: 'test',
      component: Test,
    },
  ],
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth);

  await authStore.checkAuth(); // 인증 체크

  if (requiresAuth && !authStore.isAuthenticated) {
    next('/login'); // 인증이 필요하지만 인증되지 않은 경우 로그인 페이지로 리다이렉트
  } else {
    next(); // 인증이 필요하지 않거나 인증된 경우 계속 진행
  }
});

export default router;
