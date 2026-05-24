import axios from 'axios';

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000
});

http.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload?.success === false) {
      return Promise.reject(new Error(payload.message || '请求失败'));
    }
    return payload;
  },
  (error) => Promise.reject(error)
);

export default http;
