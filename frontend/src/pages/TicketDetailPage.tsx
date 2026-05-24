import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Breadcrumbs,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  Grid,
  Link,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import TicketStatusBadge from '../components/TicketStatusBadge';
import TicketPriorityBadge from '../components/TicketPriorityBadge';
import TicketForm from '../components/TicketForm';
import CommentThread from '../components/CommentThread';
import AuditTimeline from '../components/AuditTimeline';
import { ticketsApi } from '../api/tickets';
import { usersApi } from '../api/users';
import { extractErrorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { can } from '../auth/permissions';
import type {
  CreateTicketRequest,
  Ticket,
  TicketAuditLog,
  TicketComment,
  TicketStatus,
} from '../types/ticket';
import type { User } from '../types/user';

function formatDate(iso?: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function TicketDetailPage() {
  const { id } = useParams<{ id: string }>();
  const ticketId = Number(id);
  const navigate = useNavigate();
  const { user } = useAuth();

  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [comments, setComments] = useState<TicketComment[]>([]);
  const [logs, setLogs] = useState<TicketAuditLog[]>([]);
  const [assignees, setAssignees] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [editing, setEditing] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    Promise.all([
      ticketsApi.get(ticketId),
      ticketsApi.listComments(ticketId),
      ticketsApi.listAuditLogs(ticketId),
    ])
      .then(([t, c, l]) => {
        setTicket(t);
        setComments(c);
        setLogs(l);
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [ticketId]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (can.assignTickets(user)) {
      usersApi.assignable().then(setAssignees).catch(() => setAssignees([]));
    }
  }, [user]);

  // Re-fetch everything after a mutation so audit log + ticket stay in sync.
  const runAction = async (fn: () => Promise<unknown>, reload = true): Promise<boolean> => {
    setActionError(null);
    setBusy(true);
    try {
      await fn();
      if (reload) {
        load();
      }
      return true;
    } catch (err) {
      setActionError(extractErrorMessage(err));
      return false;
    } finally {
      setBusy(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) return <Alert severity="error">{error}</Alert>;
  if (!ticket) return null;

  const canEdit = can.editTicket(user, ticket);
  const canAssign = can.assignTickets(user);
  const canComment = can.commentOnTicket(user, ticket);
  const canChangeStatus = can.changeStatus(user, ticket);
  const nextStatuses = nextStatusActions(user, ticket, canChangeStatus);

  const handleEditSubmit = async (payload: CreateTicketRequest) => {
    const ok = await runAction(() =>
      ticketsApi.update(ticket.id, {
        title: payload.title,
        description: payload.description,
        priority: payload.priority,
        dueAt: payload.dueAt,
      }),
    );
    if (ok) {
      setEditing(false);
    }
  };

  return (
    <Stack spacing={2}>
      <Breadcrumbs>
        <Link component="button" underline="hover" onClick={() => navigate('/tickets')}>
          Tickets
        </Link>
        <Typography color="text.primary">#{ticket.id}</Typography>
      </Breadcrumbs>

      <Stack direction="row" justifyContent="space-between" alignItems="flex-start" flexWrap="wrap" useFlexGap>
        <Box>
          <Typography variant="h5">{ticket.title}</Typography>
          <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
            <TicketStatusBadge status={ticket.status} />
            <TicketPriorityBadge priority={ticket.priority} />
          </Stack>
        </Box>
        <Stack direction="row" spacing={1}>
          {canEdit && !editing && (
            <Button variant="outlined" onClick={() => setEditing(true)} disabled={busy}>
              Edit
            </Button>
          )}
          {can.viewAllTickets(user) && (
            <Button color="error" variant="outlined" onClick={() => setDeleteOpen(true)} disabled={busy}>
              Delete
            </Button>
          )}
        </Stack>
      </Stack>

      {actionError && <Alert severity="error" onClose={() => setActionError(null)}>{actionError}</Alert>}

      <Grid container spacing={2}>
        {/* Main column */}
        <Grid item xs={12} md={8}>
          <Stack spacing={2}>
            <Card variant="outlined">
              <CardContent>
                {editing ? (
                  <TicketForm
                    initial={ticket}
                    submitting={busy}
                    submitLabel="Save changes"
                    canChangePriority={can.changePriority(user)}
                    onSubmit={handleEditSubmit}
                    onCancel={() => setEditing(false)}
                  />
                ) : (
                  <>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                      Description
                    </Typography>
                    <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                      {ticket.description}
                    </Typography>
                  </>
                )}
              </CardContent>
            </Card>

            <Card variant="outlined">
              <CardContent>
                <Typography variant="subtitle1" fontWeight={700} gutterBottom>
                  Comments
                </Typography>
                <CommentThread
                  comments={comments}
                  canComment={canComment}
                  submitting={busy}
                  onAdd={(b) => runAction(() => ticketsApi.addComment(ticket.id, b))}
                />
              </CardContent>
            </Card>
          </Stack>
        </Grid>

        {/* Sidebar */}
        <Grid item xs={12} md={4}>
          <Stack spacing={2}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="subtitle1" fontWeight={700} gutterBottom>
                  Details
                </Typography>
                <Stack spacing={1.25} divider={<Divider flexItem />}>
                  <Detail label="Requester" value={ticket.requester.fullName} />
                  <Detail label="Assignee" value={ticket.assignee?.fullName ?? 'Unassigned'} />
                  <Detail label="Created" value={formatDate(ticket.createdAt)} />
                  <Detail label="Updated" value={formatDate(ticket.updatedAt)} />
                  <Detail label="Due" value={formatDate(ticket.dueAt)} />
                  {ticket.resolvedAt && <Detail label="Resolved" value={formatDate(ticket.resolvedAt)} />}
                </Stack>

                {nextStatuses.length > 0 && (
                  <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mt: 2 }}>
                    {nextStatuses.map((status) => (
                      <Button
                        key={status}
                        variant="outlined"
                        disabled={busy}
                        onClick={() => runAction(() => ticketsApi.changeStatus(ticket.id, { status }))}
                      >
                        {status.replace('_', ' ')}
                      </Button>
                    ))}
                  </Stack>
                )}

                {canAssign && (
                  <TextField
                    select
                    label="Assign to"
                    size="small"
                    fullWidth
                    sx={{ mt: 1.5 }}
                    value={ticket.assignee?.id ?? ''}
                    disabled={busy}
                    onChange={(e) =>
                      runAction(() =>
                        ticketsApi.assign(ticket.id, { assigneeId: Number(e.target.value) }),
                      )
                    }
                  >
                    {assignees.map((a) => (
                      <MenuItem key={a.id} value={a.id}>
                        {a.fullName}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              </CardContent>
            </Card>

            <Card variant="outlined">
              <CardContent>
                <Typography variant="subtitle1" fontWeight={700} gutterBottom>
                  Activity
                </Typography>
                <AuditTimeline logs={logs} />
              </CardContent>
            </Card>
          </Stack>
        </Grid>
      </Grid>

      <Dialog open={deleteOpen} onClose={() => setDeleteOpen(false)}>
        <DialogTitle>Delete ticket #{ticket.id}?</DialogTitle>
        <DialogContent>
          <Typography variant="body2">
            This permanently removes the ticket and its history. This cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteOpen(false)}>Cancel</Button>
          <Button
            color="error"
            variant="contained"
            disabled={busy}
            onClick={async () => {
              const ok = await runAction(() => ticketsApi.remove(ticket.id), false);
              if (ok) {
                navigate('/tickets', { replace: true });
              }
            }}
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  );
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <Stack direction="row" justifyContent="space-between" spacing={1}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body2" fontWeight={600} sx={{ textAlign: 'right' }}>
        {value}
      </Typography>
    </Stack>
  );
}

function nextStatusActions(user: User | null, ticket: Ticket, canChangeStatus: boolean): TicketStatus[] {
  if (!canChangeStatus || !user) return [];
  switch (ticket.status) {
    case 'OPEN':
      return ['IN_PROGRESS'];
    case 'IN_PROGRESS':
      return ['RESOLVED'];
    case 'RESOLVED':
      return can.reopenTickets(user) ? ['CLOSED', 'REOPENED'] : [];
    case 'REOPENED':
      return ['IN_PROGRESS'];
    case 'CLOSED':
      return can.reopenTickets(user) ? ['REOPENED'] : [];
    default:
      return [];
  }
}
