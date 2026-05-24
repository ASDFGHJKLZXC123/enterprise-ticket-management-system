import { Chip } from '@mui/material';
import type { TicketStatus } from '../types/ticket';

type ChipColor = 'default' | 'info' | 'warning' | 'success' | 'secondary';

const CONFIG: Record<TicketStatus, { label: string; color: ChipColor }> = {
  OPEN: { label: 'Open', color: 'info' },
  IN_PROGRESS: { label: 'In Progress', color: 'warning' },
  RESOLVED: { label: 'Resolved', color: 'success' },
  CLOSED: { label: 'Closed', color: 'default' },
  REOPENED: { label: 'Reopened', color: 'secondary' },
};

export default function TicketStatusBadge({ status }: { status: TicketStatus }) {
  const { label, color } = CONFIG[status];
  return <Chip size="small" variant="outlined" color={color} label={label} />;
}
