import { api } from './client';
import type {
  AssignTicketRequest,
  ChangeTicketStatusRequest,
  CreateTicketRequest,
  Page,
  Ticket,
  TicketAuditLog,
  TicketComment,
  TicketFilters,
  UpdateTicketRequest,
} from '../types/ticket';

function toParams(filters: TicketFilters): Record<string, string | number> {
  const params: Record<string, string | number> = {};
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== '' && value !== undefined && value !== null) {
      params[key] = value as string | number;
    }
  });
  return params;
}

export const ticketsApi = {
  list: (filters: TicketFilters = {}) =>
    api.get<Page<Ticket>>('/tickets', { params: toParams(filters) }).then((r) => r.data),

  get: (id: number) => api.get<Ticket>(`/tickets/${id}`).then((r) => r.data),

  create: (payload: CreateTicketRequest) =>
    api.post<Ticket>('/tickets', payload).then((r) => r.data),

  update: (id: number, payload: UpdateTicketRequest) =>
    api.patch<Ticket>(`/tickets/${id}`, payload).then((r) => r.data),

  remove: (id: number) => api.delete<void>(`/tickets/${id}`).then((r) => r.data),

  assign: (id: number, payload: AssignTicketRequest) =>
    api.post<Ticket>(`/tickets/${id}/assign`, payload).then((r) => r.data),

  changeStatus: (id: number, payload: ChangeTicketStatusRequest) =>
    api.post<Ticket>(`/tickets/${id}/status`, payload).then((r) => r.data),

  listComments: (id: number) =>
    api.get<TicketComment[]>(`/tickets/${id}/comments`).then((r) => r.data),

  addComment: (id: number, body: string) =>
    api.post<TicketComment>(`/tickets/${id}/comments`, { body }).then((r) => r.data),

  listAuditLogs: (id: number) =>
    api.get<TicketAuditLog[]>(`/tickets/${id}/audit-logs`).then((r) => r.data),
};
