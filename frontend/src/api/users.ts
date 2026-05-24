import { api } from './client';
import type { CreateUserRequest, UpdateUserRequest, User } from '../types/user';

export const usersApi = {
  list: () => api.get<User[]>('/users').then((r) => r.data),

  assignable: () => api.get<User[]>('/users/assignable').then((r) => r.data),

  create: (payload: CreateUserRequest) =>
    api.post<User>('/users', payload).then((r) => r.data),

  update: (userId: number, payload: UpdateUserRequest) =>
    api.patch<User>(`/users/${userId}`, payload).then((r) => r.data),

  deactivate: (userId: number) =>
    api.patch<User>(`/users/${userId}/deactivate`).then((r) => r.data),

  reactivate: (userId: number) =>
    api.patch<User>(`/users/${userId}/reactivate`).then((r) => r.data),
};
