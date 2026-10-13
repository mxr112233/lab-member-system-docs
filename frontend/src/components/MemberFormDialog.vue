<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getErrorMessage } from '../api/http'
import { createMember, getMember, updateMember } from '../api/members'
import {
  DEPARTMENT_OPTIONS,
  GENDER_OPTIONS,
  POSITION_OPTIONS,
  PROFILE_STATUS_OPTIONS,
  STATUS_OPTIONS,
} from '../constants/member'
import type {
  Department,
  Gender,
  Member,
  MemberFormModel,
  MemberPayload,
  MemberStatus,
  Position,
  ProfileStatus,
} from '../types/member'

const props = defineProps<{
  modelValue: boolean
  member: Member | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  saved: []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const form = reactive<MemberFormModel>(createEmptyForm())

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value),
})

const dialogTitle = computed(() => (props.member ? '编辑成员' : '新增成员'))

watch(
  () => props.modelValue,
  async (open) => {
    if (!open) {
      return
    }
    await loadFormData()
  },
)

async function loadFormData() {
  loading.value = true
  try {
    if (props.member) {
      const current = await getMember(props.member.id)
      Object.assign(form, toForm(current))
    } else {
      Object.assign(form, createEmptyForm())
    }
    await nextTick()
    formRef.value?.clearValidate()
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
    visible.value = false
  } finally {
    loading.value = false
  }
}

function createEmptyForm(): MemberFormModel {
  return {
    name: '',
    studentNo: '',
    gender: null,
    grade: '',
    major: '',
    phone: '',
    email: '',
    department: '',
    position: '',
    profileStatus: 'PENDING',
    status: 1,
  }
}

function toForm(member: Member): MemberFormModel {
  return {
    name: member.name,
    studentNo: member.studentNo,
    gender: member.gender,
    grade: member.grade,
    major: member.major,
    phone: member.phone ?? '',
    email: member.email ?? '',
    department: member.department,
    position: member.position,
    profileStatus: member.profileStatus,
    status: member.status,
  }
}

function validateGender(
  _rule: unknown,
  value: Gender | null,
  callback: (error?: string | Error) => void,
) {
  if (value === null || value < 0 || value > 2) {
    callback(new Error('性别不合法'))
    return
  }
  callback()
}

function validateStatus(
  _rule: unknown,
  value: MemberStatus | null,
  callback: (error?: string | Error) => void,
) {
  if (value === null || value < 0 || value > 1) {
    callback(new Error('成员状态不合法'))
    return
  }
  callback()
}

const rules: FormRules<MemberFormModel> = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 50, message: '姓名长度不能超过50', trigger: 'blur' },
  ],
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { max: 30, message: '学号长度不能超过30', trigger: 'blur' },
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' },
    { validator: validateGender, trigger: 'change' },
  ],
  grade: [
    { required: true, message: '请输入年级', trigger: 'blur' },
    { max: 20, message: '年级长度不能超过20', trigger: 'blur' },
  ],
  major: [
    { required: true, message: '请输入专业', trigger: 'blur' },
    { max: 100, message: '专业长度不能超过100', trigger: 'blur' },
  ],
  phone: [
    { max: 20, message: '手机号长度不能超过20', trigger: 'blur' },
    { pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式错误', trigger: 'blur' },
  ],
  email: [
    { max: 100, message: '邮箱长度不能超过100', trigger: 'blur' },
    {
      pattern: /^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/,
      message: '邮箱格式错误',
      trigger: 'blur',
    },
  ],
  department: [{ required: true, message: '请选择部门', trigger: 'change' }],
  position: [{ required: true, message: '请选择职位', trigger: 'change' }],
  profileStatus: [{ required: true, message: '请选择资料状态', trigger: 'change' }],
  status: [
    { required: true, message: '请选择成员状态', trigger: 'change' },
    { validator: validateStatus, trigger: 'change' },
  ],
}

function toPayload(): MemberPayload {
  return {
    name: form.name.trim(),
    studentNo: form.studentNo.trim(),
    gender: form.gender as Gender,
    grade: form.grade.trim(),
    major: form.major.trim(),
    phone: form.phone.trim(),
    email: form.email.trim(),
    department: form.department as Department,
    position: form.position as Position,
    profileStatus: form.profileStatus as ProfileStatus,
    status: form.status as MemberStatus,
  }
}

async function handleSubmit() {
  if (!formRef.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload = toPayload()
    if (props.member) {
      await updateMember(props.member.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createMember(payload)
      ElMessage.success('新增成功')
    }
    emit('saved')
    visible.value = false
  } catch (error) {
    ElMessage.error(getErrorMessage(error))
  } finally {
    saving.value = false
  }
}

function handleClosed() {
  Object.assign(form, createEmptyForm())
  formRef.value?.clearValidate()
}
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="760px"
    :close-on-click-modal="false"
    destroy-on-close
    @closed="handleClosed"
  >
    <el-form
      ref="formRef"
      v-loading="loading"
      :model="form"
      :rules="rules"
      label-width="90px"
      status-icon
    >
      <el-row :gutter="18">
        <el-col :xs="24" :sm="12">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="请输入姓名" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="学号" prop="studentNo">
            <el-input
              v-model="form.studentNo"
              maxlength="30"
              show-word-limit
              placeholder="请输入学号"
            />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="性别" prop="gender">
            <el-select v-model="form.gender" placeholder="请选择性别">
              <el-option
                v-for="option in GENDER_OPTIONS"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="年级" prop="grade">
            <el-input v-model="form.grade" maxlength="20" placeholder="例如：2026" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="专业" prop="major">
            <el-input v-model="form.major" maxlength="100" placeholder="请输入专业" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" maxlength="20" placeholder="选填" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" maxlength="100" placeholder="选填" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="部门" prop="department">
            <el-select v-model="form.department" placeholder="请选择部门">
              <el-option
                v-for="option in DEPARTMENT_OPTIONS"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="职位" prop="position">
            <el-select v-model="form.position" placeholder="请选择职位">
              <el-option
                v-for="option in POSITION_OPTIONS"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="资料状态" prop="profileStatus">
            <el-select v-model="form.profileStatus" placeholder="请选择资料状态">
              <el-option
                v-for="option in PROFILE_STATUS_OPTIONS"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="12">
          <el-form-item label="成员状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择成员状态">
              <el-option
                v-for="option in STATUS_OPTIONS"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.el-select {
  width: 100%;
}
</style>