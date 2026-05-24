import { useState } from 'react';
import {
  Avatar,
  Box,
  Button,
  Divider,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import type { TicketComment } from '../types/ticket';

interface CommentThreadProps {
  comments: TicketComment[];
  canComment: boolean;
  submitting?: boolean;
  onAdd: (body: string) => void;
}

function initials(name: string): string {
  return name.split(' ').map((p) => p[0]).slice(0, 2).join('').toUpperCase();
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function CommentThread({
  comments,
  canComment,
  submitting = false,
  onAdd,
}: CommentThreadProps) {
  const [body, setBody] = useState('');

  const handleAdd = () => {
    if (body.trim().length === 0) return;
    onAdd(body.trim());
    setBody('');
  };

  return (
    <Stack spacing={2}>
      {comments.length === 0 && (
        <Typography variant="body2" color="text.secondary">
          No comments yet.
        </Typography>
      )}
      <Stack spacing={1.5}>
        {comments.map((c) => (
          <Stack key={c.id} direction="row" spacing={1.5} alignItems="flex-start">
            <Avatar sx={{ width: 28, height: 28, fontSize: 12, bgcolor: 'secondary.main' }}>
              {initials(c.author.fullName)}
            </Avatar>
            <Box sx={{ flexGrow: 1 }}>
              <Stack direction="row" spacing={1} alignItems="baseline">
                <Typography variant="body2" fontWeight={600}>
                  {c.author.fullName}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {formatTime(c.createdAt)}
                </Typography>
              </Stack>
              <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                {c.body}
              </Typography>
            </Box>
          </Stack>
        ))}
      </Stack>

      {canComment && (
        <>
          <Divider />
          <Stack spacing={1}>
            <TextField
              placeholder="Add a comment…"
              value={body}
              onChange={(e) => setBody(e.target.value)}
              multiline
              minRows={2}
              fullWidth
            />
            <Box>
              <Button
                variant="contained"
                onClick={handleAdd}
                disabled={submitting || body.trim().length === 0}
              >
                Comment
              </Button>
            </Box>
          </Stack>
        </>
      )}
    </Stack>
  );
}
