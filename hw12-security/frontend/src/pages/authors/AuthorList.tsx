import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.ts';
import type { AuthorDto } from '../../types.ts';

export default function AuthorList() {
  const [authors, setAuthors] = useState<AuthorDto[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.authors.list().then(setAuthors).catch((e: Error) => setError(e.message));
  }, []);

  return (
    <div>
      <h1>Authors</h1>
      <Link to="/authors/new">Add new author</Link>
      {error && <p className="error-message">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Full name</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {authors.map((author) => (
            <tr key={author.id}>
              <td>{author.fullName}</td>
              <td className="actions">
                <Link to={`/authors/${author.id}/edit`}>Edit</Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
