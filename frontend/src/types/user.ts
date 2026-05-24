export type Role = 'ADMIN' | 'MANAGER' | 'AGENT' | 'REQUESTER';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  fullName: string;
  role: Role;
}

export interface UpdateUserRequest {
  fullName?: string;
  role?: Role;
  active?: boolean;
}

