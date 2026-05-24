import type { User } from './user';

export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'REOPENED';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface Ticket {
  id: number;
  title: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  requester: User;
  assignee?: User | null;
  dueAt?: string | null;
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string | null;
}

export interface TicketComment {
  id: number;
  ticketId: number;
  author: User;
  body: string;
  createdAt: string;
}

export interface TicketAuditLog {
  id: number;
  ticketId: number;
  actor?: User | null;
  action: string;
  oldValue?: string | null;
  newValue?: string | null;
  createdAt: string;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  priority: TicketPriority;
  dueAt?: string | null;
}

export interface UpdateTicketRequest {
  title?: string;
  description?: string;
  priority?: TicketPriority;
  dueAt?: string | null;
}

export interface AssignTicketRequest {
  assigneeId: number;
}

export interface ChangeTicketStatusRequest {
  status: TicketStatus;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface TicketFilters {
  page?: number;
  size?: number;
  status?: TicketStatus | '';
  priority?: TicketPriority | '';
  assigneeId?: number | '';
  requesterId?: number | '';
  search?: string;
}

