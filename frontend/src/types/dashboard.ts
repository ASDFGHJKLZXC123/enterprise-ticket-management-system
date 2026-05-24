import type { Ticket, TicketPriority, TicketStatus } from './ticket';

export interface DashboardSummary {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  resolvedTickets: number;
  closedTickets: number;
  highPriorityTickets: number;
  overdueTickets: number;
  recentTickets: Ticket[];
}

export interface CountResponse<T extends string = string> {
  key: T;
  count: number;
}

export type StatusCount = CountResponse<TicketStatus>;
export type PriorityCount = CountResponse<TicketPriority>;
export type AssigneeCount = CountResponse<string>;

