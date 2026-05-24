import { api } from './client';
import type {
  AssigneeCount,
  DashboardSummary,
  PriorityCount,
  StatusCount,
} from '../types/dashboard';

export const dashboardApi = {
  summary: () => api.get<DashboardSummary>('/dashboard/summary').then((r) => r.data),

  byStatus: () =>
    api.get<StatusCount[]>('/dashboard/tickets-by-status').then((r) => r.data),

  byPriority: () =>
    api.get<PriorityCount[]>('/dashboard/tickets-by-priority').then((r) => r.data),

  byAssignee: () =>
    api.get<AssigneeCount[]>('/dashboard/tickets-by-assignee').then((r) => r.data),
};
