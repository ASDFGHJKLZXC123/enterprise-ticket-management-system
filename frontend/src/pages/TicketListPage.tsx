import { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { Plus } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import TicketTable from '../components/TicketTable';
import { ticketsApi } from '../api/tickets';
import { usersApi } from '../api/users';
import { extractErrorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { can } from '../auth/permissions';
import type { Page, Ticket, TicketFilters, TicketPriority, TicketStatus } from '../types/ticket';
import type { User } from '../types/user';

const STATUSES: TicketStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REOPENED'];
const PRIORITIES: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

export default function TicketListPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const showAssigneeFilter = can.viewAllTickets(user);

  const [filters, setFilters] = useState<TicketFilters>({ page: 0, size: 20, status: '', priority: '', assigneeId: '', search: '' });
  const [data, setData] = useState<Page<Ticket> | null>(null);
  const [assignees, setAssignees] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Debounced free-text search so we don't fire a request per keystroke.
  const [searchInput, setSearchInput] = useState('');
  useEffect(() => {
    const id = setTimeout(() => {
      setFilters((f) => ({ ...f, search: searchInput, page: 0 }));
    }, 350);
    return () => clearTimeout(id);
  }, [searchInput]);

  useEffect(() => {
    if (!showAssigneeFilter) return;
    usersApi.assignable().then(setAssignees).catch(() => setAssignees([]));
  }, [showAssigneeFilter]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    ticketsApi
      .list(filters)
      .then((d) => active && setData(d))
      .catch((err) => active && setError(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, [filters]);

  const update = (patch: Partial<TicketFilters>) =>
    setFilters((f) => ({ ...f, ...patch, page: 'page' in patch ? (patch.page as number) : 0 }));

  const body = useMemo(() => {
    if (loading && !data) {
      return (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress />
        </Box>
      );
    }
    if (error) return <Alert severity="error">{error}</Alert>;
    if (!data) return null;
    return (
      <TicketTable
        page={data}
        onRowClick={(t) => navigate(`/tickets/${t.id}`)}
        onPageChange={(p) => update({ page: p })}
        onRowsPerPageChange={(size) => update({ size, page: 0 })}
      />
    );
  }, [loading, data, error, navigate]);

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center">
        <Typography variant="h5">Tickets</Typography>
        <Button variant="contained" startIcon={<Plus size={16} />} onClick={() => navigate('/tickets/new')}>
          New ticket
        </Button>
      </Stack>

      <Card variant="outlined">
        <CardContent sx={{ py: 1.5 }}>
          <Stack direction="row" spacing={1.5} flexWrap="wrap" useFlexGap>
            <TextField
              label="Search"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              sx={{ minWidth: 200 }}
            />
            <TextField
              select
              label="Status"
              value={filters.status ?? ''}
              onChange={(e) => update({ status: e.target.value as TicketStatus | '' })}
              sx={{ minWidth: 150 }}
            >
              <MenuItem value="">All statuses</MenuItem>
              {STATUSES.map((s) => (
                <MenuItem key={s} value={s}>
                  {s.replace('_', ' ')}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Priority"
              value={filters.priority ?? ''}
              onChange={(e) => update({ priority: e.target.value as TicketPriority | '' })}
              sx={{ minWidth: 150 }}
            >
              <MenuItem value="">All priorities</MenuItem>
              {PRIORITIES.map((p) => (
                <MenuItem key={p} value={p}>
                  {p}
                </MenuItem>
              ))}
            </TextField>
            {showAssigneeFilter && (
              <TextField
                select
                label="Assignee"
                value={filters.assigneeId ?? ''}
                onChange={(e) =>
                  update({ assigneeId: e.target.value === '' ? '' : Number(e.target.value) })
                }
                sx={{ minWidth: 180 }}
              >
                <MenuItem value="">All assignees</MenuItem>
                {assignees.map((a) => (
                  <MenuItem key={a.id} value={a.id}>
                    {a.fullName}
                  </MenuItem>
                ))}
              </TextField>
            )}
          </Stack>
        </CardContent>
      </Card>

      {body}
    </Stack>
  );
}
