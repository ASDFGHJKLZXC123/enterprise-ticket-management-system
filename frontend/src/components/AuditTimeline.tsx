import { Box, Stack, Typography } from '@mui/material';
import type { TicketAuditLog } from '../types/ticket';

const ACTION_LABELS: Record<string, string> = {
  TICKET_CREATED: 'created the ticket',
  TICKET_ASSIGNED: 'assigned the ticket',
  STATUS_CHANGED: 'changed status',
  PRIORITY_CHANGED: 'changed priority',
  COMMENT_ADDED: 'added a comment',
  TICKET_CLOSED: 'closed the ticket',
  TICKET_REOPENED: 'reopened the ticket',
  USER_DEACTIVATED: 'deactivated a user',
};

function formatTime(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function describe(log: TicketAuditLog): string {
  const base = ACTION_LABELS[log.action] ?? log.action;
  if (log.oldValue && log.newValue) return `${base}: ${log.oldValue} → ${log.newValue}`;
  if (log.newValue) return `${base}: ${log.newValue}`;
  return base;
}

export default function AuditTimeline({ logs }: { logs: TicketAuditLog[] }) {
  if (logs.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No activity recorded.
      </Typography>
    );
  }

  return (
    <Stack spacing={0}>
      {logs.map((log, idx) => (
        <Stack key={log.id} direction="row" spacing={1.5}>
          {/* Timeline rail: dot + connecting line */}
          <Stack alignItems="center" sx={{ width: 16 }}>
            <Box
              sx={{
                width: 9,
                height: 9,
                borderRadius: '50%',
                bgcolor: 'primary.main',
                mt: 0.6,
                flexShrink: 0,
              }}
            />
            {idx < logs.length - 1 && (
              <Box sx={{ width: '2px', flexGrow: 1, bgcolor: 'divider', minHeight: 16 }} />
            )}
          </Stack>
          <Box sx={{ pb: 1.5 }}>
            <Typography variant="body2">
              <strong>{log.actor?.fullName ?? 'System'}</strong> {describe(log)}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {formatTime(log.createdAt)}
            </Typography>
          </Box>
        </Stack>
      ))}
    </Stack>
  );
}
