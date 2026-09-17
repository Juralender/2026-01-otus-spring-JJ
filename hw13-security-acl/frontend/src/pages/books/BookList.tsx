import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.ts';
import type { BookDto } from '../../types.ts';

export default function BookList() {
  const [books, setBooks] = useState<BookDto[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = () => {
    api.books.list().then(setBooks).catch((e: Error) => setError(e.message));
  };

  useEffect(load, []);

  const handleDelete = (id: number) => {
    if (!window.confirm('Delete this book?')) return;
    api.books.remove(id).then(load).catch((e: Error) => setError(e.message));
  };

  return (
    <div>
      <h1>Books</h1>
      <Link to="/books/new">Add new book</Link>
      {error && <p className="error-message">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Title</th>
            <th>Author</th>
            <th>Genres</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {books.map((book) => (
            <tr key={book.id}>
              <td>{book.title}</td>
              <td>{book.author.fullName}</td>
              <td>{book.genres.map((g) => g.name).join(', ')}</td>
              <td className="actions">
                <Link to={`/books/${book.id}`}>View</Link>
                <Link to={`/books/${book.id}/edit`}>Edit</Link>
                <button type="button" onClick={() => handleDelete(book.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
