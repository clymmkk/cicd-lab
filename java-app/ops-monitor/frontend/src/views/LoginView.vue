<template>
  <div class="login-container">
    <div class="login-box">
      <h2>运维监控平台</h2>
      <form @submit.prevent="handleLogin">
        <div class="input-group">
          <label>用户名</label>
          <input v-model="form.username" type="text" required />
        </div>
        <div class="input-group">
          <label>密码</label>
          <input v-model="form.password" type="password" required />
        </div>
        <button type="submit" class="btn-login">登录</button>
      </form>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { login } from '../api/auth.js'

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const form = reactive({ username: 'admin', password: 'admin' })
    const error = ref('')

    const handleLogin = async () => {
      try {
        error.value = ''
        const res = await login(form.username, form.password)
        authStore.setAuth(res.token, res.username)
        router.push('/dashboard')
      } catch (err) {
        error.value = '登录失败：' + (err.response?.data?.message || err.message)
      }
    }

    return { form, error, handleLogin }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1e3a5f 0%, #2c3e50 100%);
}

.login-box {
  background: #fff;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  width: 360px;
}

.login-box h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #555;
}

.input-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.btn-login {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 6px;
  background: #3498db;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-login:hover {
  background: #2980b9;
}

.error {
  color: #e74c3c;
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
}
</style>
