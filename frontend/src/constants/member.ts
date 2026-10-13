import type {
  Department,
  Gender,
  MemberStatus,
  Position,
  ProfileStatus,
} from '../types/member'

export interface SelectOption<T> {
  value: T
  label: string
}

export const GENDER_OPTIONS: SelectOption<Gender>[] = [
  { value: 0, label: '未知' },
  { value: 1, label: '男' },
  { value: 2, label: '女' },
]

export const STATUS_OPTIONS: SelectOption<MemberStatus>[] = [
  { value: 1, label: '正常' },
  { value: 0, label: '已离开' },
]

export const DEPARTMENT_OPTIONS: SelectOption<Department>[] = [
  { value: 'SOFTWARE', label: '软件研发部' },
  { value: 'ACHIEVEMENT', label: '成果中心' },
]

export const POSITION_OPTIONS: SelectOption<Position>[] = [
  { value: 'CAPTAIN', label: '队长' },
  { value: 'VICE_CAPTAIN', label: '副队长' },
  { value: 'FRONTEND', label: '前端' },
  { value: 'BACKEND', label: '后端' },
  { value: 'FULLSTACK', label: '全栈' },
  { value: 'PRODUCT', label: '产品' },
  { value: 'QA', label: '测试' },
  { value: 'DEVOPS', label: '运维' },
  { value: 'ACHIEVEMENT', label: '成果中心' },
]

export const PROFILE_STATUS_OPTIONS: SelectOption<ProfileStatus>[] = [
  { value: 'PENDING', label: '待补录' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'ESCALATED', label: '已升级人工跟进' },
]

const GENDER_LABELS: Record<Gender, string> = {
  0: '未知',
  1: '男',
  2: '女',
}

const STATUS_LABELS: Record<MemberStatus, string> = {
  0: '已离开',
  1: '正常',
}

const DEPARTMENT_LABELS: Record<Department, string> = {
  SOFTWARE: '软件研发部',
  ACHIEVEMENT: '成果中心',
}

const POSITION_LABELS: Record<Position, string> = {
  CAPTAIN: '队长',
  VICE_CAPTAIN: '副队长',
  FRONTEND: '前端',
  BACKEND: '后端',
  FULLSTACK: '全栈',
  PRODUCT: '产品',
  QA: '测试',
  DEVOPS: '运维',
  ACHIEVEMENT: '成果中心',
}

const PROFILE_STATUS_LABELS: Record<ProfileStatus, string> = {
  PENDING: '待补录',
  COMPLETED: '已完成',
  ESCALATED: '已升级人工跟进',
}

export function getGenderLabel(value: Gender): string {
  return GENDER_LABELS[value]
}

export function getStatusLabel(value: MemberStatus): string {
  return STATUS_LABELS[value]
}

export function getDepartmentLabel(value: Department): string {
  return DEPARTMENT_LABELS[value]
}

export function getPositionLabel(value: Position): string {
  return POSITION_LABELS[value]
}

export function getProfileStatusLabel(value: ProfileStatus): string {
  return PROFILE_STATUS_LABELS[value]
}