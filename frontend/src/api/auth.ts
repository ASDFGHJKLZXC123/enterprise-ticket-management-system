import { api } from './client';
import type { LoginRequest, LoginResponse, RegisterRequest } from '../types/auth';
import type { User } from '../types/user';

export const authApi = {
  login: (payload: LoginRequest) =>
    api.post<LoginResponse>('/auth/login', payload).then((r) => r.data),

  register: (payload: RegisterRequest) =>
    api.post<LoginResponse>('/auth/register', payload).then((r) => r.data),

  me: () => api.get<User>('/auth/me').then((r) => r.data),
};
