import type { Role, User } from '../types/user';
import type { Ticket } from '../types/ticket';

/** Role helpers mirroring the backend permission model. The backend remains the
 *  source of truth; these only drive what the UI shows. */

export const can = {
  manageUsers: (u: User | null) => u?.role === 'ADMIN',
  deactivateUsers: (u: User | null) => u?.role === 'ADMIN',
  viewAllTickets: (u: User | null) => u?.role === 'ADMIN' || u?.role === 'MANAGER',
  assignTickets: (u: User | null) => u?.role === 'ADMIN' || u?.role === 'MANAGER',
  changePriority: (u: User | null) => u?.role === 'ADMIN' || u?.role === 'MANAGER',
  reopenTickets: (u: User | null) => u?.role === 'ADMIN' || u?.role === 'MANAGER',
  createTickets: (u: User | null) => !!u,
  changeStatus: (u: User | null, t: Ticket | null): boolean => {
    if (!u || !t) return false;
    if (t.status === 'CLOSED') return u.role === 'ADMIN' || u.role === 'MANAGER';
    if (u.role === 'ADMIN' || u.role === 'MANAGER') return true;
    return u.role === 'AGENT' && t.assignee?.id === u.id;
  },

  /** Whether the user may edit a ticket's content. Closed tickets must be reopened first. */
  editTicket: (u: User | null, t: Ticket | null): boolean => {
    if (!u || !t) return false;
    if (t.status === 'CLOSED') return false;
    if (u.role === 'ADMIN' || u.role === 'MANAGER') return true;
    if (u.role === 'AGENT') return t.assignee?.id === u.id;
    return false;
  },

  commentOnTicket: (u: User | null, t: Ticket | null): boolean => {
    if (!u || !t) return false;
    if (t.status === 'CLOSED') return false;
    if (u.role === 'ADMIN' || u.role === 'MANAGER') return true;
    if (u.role === 'AGENT') return t.assignee?.id === u.id;
    if (u.role === 'REQUESTER') return t.requester.id === u.id;
    return false;
  },
};

export const ROLE_LABELS: Record<Role, string> = {
  ADMIN: 'Administrator',
  MANAGER: 'Manager',
  AGENT: 'Agent',
  REQUESTER: 'Requester',
};
