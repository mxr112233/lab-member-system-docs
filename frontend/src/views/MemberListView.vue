<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteMember, listMembers } from '../api/members'
import { getErrorMessage } from '../api/http'
import MemberFormDialog from '../components/MemberFormDialog.vue'
import {
  DEPARTMENT_OPTIONS,
  POSITION_OPTIONS,
  PROFILE_STATUS_OPTIONS,
  STATUS_OPTIONS,
  getDepartmentLabel,
  getGenderLabel,
  getPositionLabel,
  getProfileStatusLabel,
  getStatusLabel,
} from '../constants/member'
import type { Member, MemberFilterModel, MemberQuery, ProfileStatus } from '../types/member'

const loading = ref(false)
const members = ref<Member[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const selectedMember = ref<Member | null>(null)
const filters = reactive<MemberFilterModel>(createFilters())

function createFilters(): MemberFilterModel {
  return {
    name: '',
    studentNo: '',
    grade: '',
    department: '',
    position: '',
    profileStatus: '',
    status: '',
  }
}

function buildQuery(): MemberQuery {
  const query: MemberQuery = {
    page: page.value,
    pageSize: pageSize.value,
  }
  if (filters.name.trim()) {
    query.name = filters.name.trim()
  }
  if (filters.studentNo.trim()) {
    query.studentNo = filters.studentNo.trim()
  }
  if (filters.grade.trim()) {
    query.grade = filters.grade.trim()
  }
  if (filters.department) {
    query.department = filters.department
  }
  if (filters.position) {
    query.position = filters.position
  }
  if (filters.profileStatus) {
    query.profileStatus = filters.profileStatus
  }
  if (filters.status !== '') {
    query.status = filters.status
  }
  return query
}

async function loadMembers() {
  loading.value = true
  try {
    const result = await listMembers(buildQuery())
    members.value = result.records
    total.value = result.total
    page.value = result.page
    pageSize.value = result.pageSize
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  page.value = 1
  await loadMembers()
}

async function handleReset() {
  Object.assign(filters, createFilters())
  page.value = 1
  await loadMembers()
}

async function handleSizeChange() {
  page.value = 1
  await loadMembers()
}

function openCreate() {
  selectedMember.value = null
  dialogVisible.value = true
}

function openEdit(member: Member) {
  selectedMember.value = member
  dialogVisible.value = true
}

async function handleSaved() {
  if (!selectedMember.value) {
    page.value = 1
  }
  await loadMembers()
}

async function handleDelete(member: Member) {
  try {
    await ElMessageBox.confirm(
      `确定删除成员“${member.name}”（${member.studentNo}）吗？`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  try {
    await deleteMember(member.id)
    ElMessage.success('删除成功')
    if (members.value.length === 1 && page.value > 1) {
      page.value -= 1
    }
    await loadMembers()
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  }
}

function getProfileStatusTagType(value: ProfileStatus) {
  if (value === 'COMPLETED') {
    return 'success'
  }
  if (value === 'ESCALATED') {
    return 'danger'
  }
  return 'warning'
}

onMounted(loadMembers)
</script>

<template>
  <main class="member-page">
    <header class="page-header">
      <div>
        <h1>实验室成员管理系统</h1>
        <p>维护成员基础信息、部门职位和资料状态。</p>
      </div>
      <el-button type="primary" size="large" @click="openCreate">新增成员</el-button>
    </header>

    <el-card class="filter-card" shadow="never">
      <el-form :model="filters" label-width="72px" @submit.prevent>
        <el-row :gutter="14">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="姓名">
              <el-input v-model="filters.name" clearable placeholder="姓名模糊查询" @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="学号">
              <el-input v-model="filters.studentNo" clearable placeholder="精确查询" @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="年级">
              <el-input v-model="filters.grade" clearable placeholder="例如：2026" @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="部门">
              <el-select v-model="filters.department" clearable placeholder="全部部门">
                <el-option
                  v-for="option in DEPARTMENT_OPTIONS"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="职位">
              <el-select v-model="filters.position" clearable placeholder="全部职位">
                <el-option
                  v-for="option in POSITION_OPTIONS"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="资料状态">
              <el-select v-model="filters.profileStatus" clearable placeholder="全部资料状态">
                <el-option
                  v-for="option in PROFILE_STATUS_OPTIONS"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="成员状态">
              <el-select v-model="filters.status" clearable placeholder="全部成员状态">
                <el-option
                  v-for="option in STATUS_OPTIONS"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item class="filter-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="members" border stripe row-key="id">
        <el-table-column prop="id" label="ID" width="72" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column label="性别" width="74">
          <template #default="{ row }">{{ getGenderLabel(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="86" />
        <el-table-column prop="major" label="专业" min-width="150" show-overflow-tooltip />
        <el-table-column label="手机号" min-width="130">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column label="邮箱" min-width="190" show-overflow-tooltip>
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column label="部门" min-width="120">
          <template #default="{ row }">{{ getDepartmentLabel(row.department) }}</template>
        </el-table-column>
        <el-table-column label="职位" min-width="110">
          <template #default="{ row }">{{ getPositionLabel(row.position) }}</template>
        </el-table-column>
        <el-table-column label="资料状态" min-width="120">
          <template #default="{ row }">
            <el-tag :type="getProfileStatusTagType(row.profileStatus)" effect="light">
              {{ getProfileStatusLabel(row.profileStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成员状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadMembers"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <MemberFormDialog v-model="dialogVisible" :member="selectedMember" @saved="handleSaved" />
  </main>
</template>

<style scoped>
.member-page {
  min-height: 100vh;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 18px;
}

.page-header h1 {
  margin: 0 0 6px;
  font-size: 26px;
  color: #111827;
}

.page-header p {
  margin: 0;
  color: #64748b;
}

.filter-card,
.table-card {
  border: 0;
  border-radius: 14px;
}

.filter-card {
  margin-bottom: 18px;
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-card :deep(.el-select) {
  width: 100%;
}

.filter-actions :deep(.el-form-item__content) {
  flex-wrap: nowrap;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 18px;
}

@media (max-width: 767px) {
  .member-page {
    padding: 14px;
  }

  .page-header {
    align-items: stretch;
    flex-direction: column;
  }

  .filter-card :deep(.el-form-item) {
    margin-bottom: 12px;
  }

  .pagination-wrap {
    justify-content: center;
    overflow-x: auto;
  }
}
</style>