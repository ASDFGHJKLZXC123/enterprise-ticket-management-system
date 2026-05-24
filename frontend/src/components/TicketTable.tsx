import {
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  Typography,
} from '@mui/material';
import TicketStatusBadge from './TicketStatusBadge';
import TicketPriorityBadge from './TicketPriorityBadge';
import type { Page, Ticket } from '../types/ticket';

interface TicketTableProps {
  page: Page<Ticket>;
  onRowClick: (ticket: Ticket) => void;
  onPageChange: (page: number) => void;
  onRowsPerPageChange: (size: number) => void;
}

function formatDate(iso?: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

export default function TicketTable({
  page,
  onRowClick,
  onPageChange,
  onRowsPerPageChange,
}: TicketTableProps) {
  return (
    <Box>
      <TableContainer>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 700 }}>ID</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>Title</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>Status</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>Priority</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>Requester</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>Assignee</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>Due</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>Updated</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {page.content.length === 0 && (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 4 }}>
                  <Typography variant="body2" color="text.secondary">
                    No tickets match the current filters.
                  </Typography>
                </TableCell>
              </TableRow>
            )}
            {page.content.map((ticket) => (
              <TableRow
                key={ticket.id}
                hover
                onClick={() => onRowClick(ticket)}
                sx={{ cursor: 'pointer' }}
              >
                <TableCell>#{ticket.id}</TableCell>
                <TableCell sx={{ maxWidth: 320 }}>
                  <Typography variant="body2" noWrap fontWeight={600}>
                    {ticket.title}
                  </Typography>
                </TableCell>
                <TableCell>
                  <TicketStatusBadge status={ticket.status} />
                </TableCell>
                <TableCell>
                  <TicketPriorityBadge priority={ticket.priority} />
                </TableCell>
                <TableCell>{ticket.requester.fullName}</TableCell>
                <TableCell>{ticket.assignee?.fullName ?? '—'}</TableCell>
                <TableCell>{formatDate(ticket.dueAt)}</TableCell>
                <TableCell>{formatDate(ticket.updatedAt)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        component="div"
        count={page.totalElements}
        page={page.number}
        rowsPerPage={page.size}
        onPageChange={(_, p) => onPageChange(p)}
        onRowsPerPageChange={(e) => onRowsPerPageChange(parseInt(e.target.value, 10))}
        rowsPerPageOptions={[10, 20, 50]}
        size="small"
      />
    </Box>
  );
}
