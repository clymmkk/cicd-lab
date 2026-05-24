<template>
  <div class="dashboard">
    <nav class="navbar">
      <div class="nav-brand">运维监控平台</div>
      <div class="nav-links">
        <router-link to="/dashboard" class="nav-link">仪表盘</router-link>
        <router-link to="/servers" class="nav-link active">服务器</router-link>
        <button class="nav-link btn-logout" @click="logout">退出</button>
      </div>
    </nav>

    <div class="content">
      <div class="header-row">
        <h1>服务器管理</h1>
        <button class="btn-add" @click="showAdd = true">新增服务器</button>
      </div>

      <div v-if="showAdd" class="add-form">
        <input v-model="newServer.name" placeholder="服务器名称" />
        <input v-model="newServer.host" placeholder="主机地址" />
        <input v-model="newServer.os" placeholder="操作系统" />
        <button @click="handleAdd" class="btn-save">保存</button>
        <button @click="showAdd = false" class="btn-cancel">取消</button>
      </div>

      <table class="server-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>主机</th>
            <th>操作系统</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="server in servers" :key="server.id">
            <td>{{ server.id }}</td>
            <td>{{ server.name }}</td>
            <td>{{ server.host }}</td>
            <td>{{ server.os }}</td>
            <td>
              <span class="status" :class="server.status.toLowerCase()">{{ server.status }}</span>
            </td>
            <td>
              <button class="btn-delete" @click="handleDelete(server.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { getServers, addServer, deleteServer } from '../api/server.js'

export default {
  name: 'ServerListView',
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const servers = ref([])
    const showAdd = ref(false)
    const newServer = reactive({ name: '', host: '', os: '' })

    const logout = () => {
      authStore.logout()
      router.push('/login')
    }

    const loadServers = async () => {
      try {
        servers.value = await getServers()
      } catch (err) {
        console.error('获取服务器列表失败', err)
      }
    }

    const handleAdd = async () => {
      try {
        await addServer(newServer)
        newServer.name = ''
        newServer.host = ''
        newServer.os = ''
        showAdd.value = false
        await loadServers()
      } catch (err) {
        alert('添加失败: ' + (err.response?.data?.message || err.message))
      }
    }

    const handleDelete = async (id) => {
      if (!confirm('确认删除该服务器?')) return
      try {
        await deleteServer(id)
        await loadServers()
      } catch (err) {
        alert('删除失败: ' + (err.response?.data?.message || err.message))
      }
    }

    onMounted(loadServers)

    return { servers, showAdd, newServer, logout, handleAdd, handleDelete }
  }
}
</script>

<style scoped>
.dashboard { min-height: 100vh; }

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  height: 60px;
  background: #2c3e50;
  color: #fff;
}

.nav-brand { font-size: 20px; font-weight: bold; }

.nav-links {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-link {
  color: #bdc3c7;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
  background: none;
  border: none;
  cursor: pointer;
}

.nav-link:hover, .nav-link.active { color: #fff; }

.btn-logout { color: #e74c3c; }

.content { padding: 30px 40px; }

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.btn-add {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  background: #27ae60;
  color: #fff;
  cursor: pointer;
  font-size: 14px;
}

.btn-add:hover { background: #219150; }

.add-form {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.add-form input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  flex: 1;
}

.btn-save, .btn-cancel {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-save { background: #27ae60; color: #fff; }
.btn-cancel { background: #95a5a6; color: #fff; }

.server-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.server-table th,
.server-table td {
  padding: 14px 20px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.server-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #555;
  font-size: 14px;
}

.status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status.online { background: #d4edda; color: #155724; }
.status.offline { background: #f8d7da; color: #721c24; }

.btn-delete {
  padding: 6px 14px;
  border: none;
  border-radius: 4px;
  background: #e74c3c;
  color: #fff;
  cursor: pointer;
  font-size: 13px;
}

.btn-delete:hover { background: #c0392b; }
</style>
