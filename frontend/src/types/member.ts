export type Gender = 0 | 1 | 2
export type MemberStatus = 0 | 1
export type Department = 'SOFTWARE' | 'ACHIEVEMENT'
export type Position =
  | 'CAPTAIN'
  | 'VICE_CAPTAIN'
  | 'FRONTEND'
  | 'BACKEND'
  | 'FULLSTACK'
  | 'PRODUCT'
  | 'QA'
  | 'DEVOPS'
  | 'ACHIEVEMENT'
export type ProfileStatus = 'PENDING' | 'COMPLETED' | 'ESCALATED'

export interface Member {
  id: number
  name: string
  studentNo: string
  gender: Gender
  grade: string
  major: string
  phone: string | null
  email: string | null
  department: Department
  position: Position
  profileStatus: ProfileStatus
  status: MemberStatus
  createTime: string
  updateTime: string
}

export interface MemberPayload {
  name: string
  studentNo: string
  gender: Gender
  grade: string
  major: string
  phone: string
  email: string
  department: Department
  position: Position
  profileStatus: ProfileStatus
  status: MemberStatus
}

export interface MemberFormModel {
  name: string
  studentNo: string
  gender: Gender | null
  grade: string
  major: string
  phone: string
  email: string
  department: Department | ''
  position: Position | ''
  profileStatus: ProfileStatus | ''
  status: MemberStatus | null
}

export interface MemberFilterModel {
  name: string
  studentNo: string
  grade: string
  department: Department | ''
  position: Position | ''
  profileStatus: ProfileStatus | ''
  status: MemberStatus | ''
}

export interface MemberQuery {
  page: number
  pageSize: number
  name?: string
  studentNo?: string
  grade?: string
  department?: Department
  position?: Position
  profileStatus?: ProfileStatus
  status?: MemberStatus
}

export interface MemberPage {
  records: Member[]
  total: number
  page: number
  pageSize: number
}