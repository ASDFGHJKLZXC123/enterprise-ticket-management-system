import { useEffect, useState } from 'react';
import {
  Alert,
  Box,
  Card,
  CardContent,
  CircularProgress,
  Grid,
  Stack,
  Typography,
} from '@mui/material';
import {
  AlertTriangle,
  CheckCircle2,
  Clock,
  Inbox,
  Loader,
  Flame,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import DashboardCard from '../components/DashboardCard';
import TicketStatusBadge from '../components/TicketStatusBadge';
import TicketPriorityBadge from '../components/TicketPriorityBadge';
import { dashboardApi } from '../api/dashboard';
import { extractErrorMessage } from '../api/client';
import type { DashboardSummary } from '../types/dashboard';

export default function DashboardPage() {
  const navigate = useNavigate();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    setLoading(true);
    dashboardApi
      .summary()
      .then((data) => active && setSummary(data))
      .catch((err) => active && setError(extractErrorMessage(err)))
      .finally(() => active && setLoading(false));
    return () => {
      active = false;
    };
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  if (!summary) return null;

  const cards = [
    { label: 'Total', value: summary.totalTickets, icon: <Inbox size={22} />, accent: undefined },
    { label: 'Open', value: summary.openTickets, icon: <Clock size={22} />, accent: '#1f6feb' },
    { label: 'In Progress', value: summary.inProgressTickets, icon: <Loader size={22} />, accent: '#b45309' },
    { label: 'Resolved', value: summary.resolvedTickets, icon: <CheckCircle2 size={22} />, accent: '#15803d' },
    { label: 'High Priority', value: summary.highPriorityTickets, icon: <Flame size={22} />, accent: '#c2410c' },
    { label: 'Overdue', value: summary.overdueTickets, icon: <AlertTriangle size={22} />, accent: '#b91c1c' },
  ];

  return (
    <Stack spacing={2.5}>
      <Typography variant="h5">Dashboard</Typography>

      <Grid container spacing={2}>
        {cards.map((c) => (
          <Grid key={c.label} item xs={6} sm={4} md={2}>
            <DashboardCard label={c.label} value={c.value} icon={c.icon} accent={c.accent} />
          </Grid>
        ))}
      </Grid>

      <Card variant="outlined">
        <CardContent>
          <Typography variant="subtitle1" fontWeight={700} gutterBottom>
            Recent tickets
          </Typography>
          {summary.recentTickets.length === 0 ? (
            <Typography variant="body2" color="text.secondary">
              No recent tickets.
            </Typography>
          ) : (
            <Stack divider={<Box sx={{ borderBottom: 1, borderColor: 'divider' }} />}>
              {summary.recentTickets.map((t) => (
                <Stack
                  key={t.id}
                  direction="row"
                  spacing={1.5}
                  alignItems="center"
                  sx={{ py: 1, cursor: 'pointer' }}
                  onClick={() => navigate(`/tickets/${t.id}`)}
                >
                  <Typography variant="body2" color="text.secondary" sx={{ width: 48 }}>
                    #{t.id}
                  </Typography>
                  <Typography variant="body2" fontWeight={600} sx={{ flexGrow: 1 }} noWrap>
                    {t.title}
                  </Typography>
                  <TicketPriorityBadge priority={t.priority} />
                  <TicketStatusBadge status={t.status} />
                </Stack>
              ))}
            </Stack>
          )}
        </CardContent>
      </Card>
    </Stack>
  );
}
