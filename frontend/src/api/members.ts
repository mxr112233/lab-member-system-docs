import { request } from './http'
import type {
  Member,
  MemberPage,
  MemberPayload,
  MemberQuery,
} from '../types/member'

export function createMember(payload: MemberPayload): Promise<{ id: number }> {
  return request<{ id: number }>({
    url: '/members',
    method: 'post',
    data: payload,
  })
}

export function getMember(id: number): Promise<Member> {
  return request<Member>({
    url: `/members/${id}`,
    method: 'get',
  })
}

export function listMembers(params: MemberQuery): Promise<MemberPage> {
  return request<MemberPage>({
    url: '/members',
    method: 'get',
    params,
  })
}

export function updateMember(id: number, payload: MemberPayload): Promise<Member> {
  return request<Member>({
    url: `/members/${id}`,
    method: 'put',
    data: payload,
  })
}

export function deleteMember(id: number): Promise<void> {
  return request<void>({
    url: `/members/${id}`,
    method: 'delete',
  })
}