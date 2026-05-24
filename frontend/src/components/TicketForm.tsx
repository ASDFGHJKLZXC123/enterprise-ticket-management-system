import { useState } from 'react';
import {
  Button,
  MenuItem,
  Stack,
  TextField,
} from '@mui/material';
import type { CreateTicketRequest, Ticket, TicketPriority } from '../types/ticket';

const PRIORITIES: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

interface TicketFormProps {
  initial?: Ticket;
  submitting?: boolean;
  submitLabel?: string;
  canChangePriority?: boolean;
  onSubmit: (payload: CreateTicketRequest) => void;
  onCancel?: () => void;
}

/** Shared create/edit form. Emits a CreateTicketRequest shape; callers can map
 *  it to UpdateTicketRequest when editing (same fields). */
export default function TicketForm({
  initial,
  submitting = false,
  submitLabel = 'Create ticket',
  canChangePriority = true,
  onSubmit,
  onCancel,
}: TicketFormProps) {
  const [title, setTitle] = useState(initial?.title ?? '');
  const [description, setDescription] = useState(initial?.description ?? '');
  const [priority, setPriority] = useState<TicketPriority>(initial?.priority ?? 'MEDIUM');
  const [dueAt, setDueAt] = useState(initial?.dueAt ? initial.dueAt.slice(0, 10) : '');
  const [touched, setTouched] = useState(false);

  const titleError = touched && title.trim().length === 0;
  const descriptionError = touched && description.trim().length === 0;

  const handleSubmit = () => {
    setTouched(true);
    if (title.trim().length === 0 || description.trim().length === 0) return;
    onSubmit({
      title: title.trim(),
      description: description.trim(),
      priority,
      dueAt: dueAt ? new Date(dueAt).toISOString() : null,
    });
  };

  return (
    <Stack spacing={2} sx={{ maxWidth: 640 }}>
      <TextField
        label="Title"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        error={titleError}
        helperText={titleError ? 'Title is required' : ' '}
        fullWidth
        required
      />
      <TextField
        label="Description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        error={descriptionError}
        helperText={descriptionError ? 'Description is required' : ' '}
        multiline
        minRows={4}
        fullWidth
        required
      />
      <Stack direction="row" spacing={2}>
        <TextField
          select
          label="Priority"
          value={priority}
          onChange={(e) => setPriority(e.target.value as TicketPriority)}
          disabled={!canChangePriority}
          sx={{ minWidth: 160 }}
        >
          {PRIORITIES.map((p) => (
            <MenuItem key={p} value={p}>
              {p.charAt(0) + p.slice(1).toLowerCase()}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          label="Due date"
          type="date"
          value={dueAt}
          onChange={(e) => setDueAt(e.target.value)}
          InputLabelProps={{ shrink: true }}
        />
      </Stack>
      <Stack direction="row" spacing={1}>
        <Button variant="contained" onClick={handleSubmit} disabled={submitting}>
          {submitLabel}
        </Button>
        {onCancel && (
          <Button variant="text" onClick={onCancel} disabled={submitting}>
            Cancel
          </Button>
        )}
      </Stack>
    </Stack>
  );
}
