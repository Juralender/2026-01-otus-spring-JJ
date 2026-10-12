import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../../api/client.ts';
import type { BookCommentDto, BookDto } from '../../types.ts';

export default function BookDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [book, setBook] = useState<BookDto | null>(null);
  const [comments, setComments] = useState<BookCommentDto[]>([]);
  const [commentText, setCommentText] = useState('');
  const [error, setError] = useState<string | null>(null);

  const loadComments = () => api.comments.list(id!).then(setComments);

  useEffect(() => {
    api.books.get(id!).then(setBook).catch((e: Error) => setError(e.message));
    loadComments().catch((e: Error) => setError(e.message));
  }, [id]);

  const handleDeleteBook = () => {
    if (!window.confirm('Delete this book?')) return;
    api.books.remove(id!).then(() => navigate('/books')).catch((e: Error) => setError(e.message));
  };

  const handleAddComment = (e: React.FormEvent) => {
    e.preventDefault();
    api.comments.create(id!, { text: commentText })
      .then(() => {
        setCommentText('');
        return loadComments();
      })
      .catch((e: Error) => setError(e.message));
  };

  const handleDeleteComment = (commentId: number) => {
    if (!window.confirm('Delete this comment?')) return;
    api.comments.remove(id!, commentId).then(loadComments).catch((e: Error) => setError(e.message));
  };

  if (error) return <p className="error-message">{error}</p>;
  if (!book) return <p>Loading...</p>;

  return (
    <div>
      <h1>{book.title}</h1>
      <p><strong>Author:</strong> {book.author.fullName}</p>
      <p><strong>Genres:</strong> {book.genres.map((g) => g.name).join(', ')}</p>

      <div className="buttons">
        <Link to={`/books/${book.id}/edit`}>Edit</Link>
        <button type="button" onClick={handleDeleteBook}>Delete</button>
        <Link to="/books">Back to books</Link>
      </div>

      <h2>Comments</h2>
      {comments.length === 0 && <p>No comments yet.</p>}
      {comments.map((comment) => (
        <div className="comment" key={comment.id}>
          <p>{comment.text}</p>
          <button type="button" onClick={() => handleDeleteComment(comment.id)}>Delete</button>
        </div>
      ))}

      <h3>Add comment</h3>
      <form onSubmit={handleAddComment}>
        <label htmlFor="text">Text</label>
        <input
          id="text"
          type="text"
          value={commentText}
          onChange={(e) => setCommentText(e.target.value)}
        />
        <div className="buttons">
          <button type="submit">Add comment</button>
        </div>
      </form>
    </div>
  );
}
