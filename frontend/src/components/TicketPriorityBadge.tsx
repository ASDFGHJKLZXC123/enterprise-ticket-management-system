import { Chip } from '@mui/material';
import type { TicketPriority } from '../types/ticket';

type ChipColor = 'default' | 'info' | 'warning' | 'error';

const CONFIG: Record<TicketPriority, { label: string; color: ChipColor }> = {
  LOW: { label: 'Low', color: 'default' },
  MEDIUM: { label: 'Medium', color: 'info' },
  HIGH: { label: 'High', color: 'warning' },
  URGENT: { label: 'Urgent', color: 'error' },
};

export default function TicketPriorityBadge({ priority }: { priority: TicketPriority }) {
  const { label, color } = CONFIG[priority];
  return <Chip size="small" color={color} label={label} />;
}
