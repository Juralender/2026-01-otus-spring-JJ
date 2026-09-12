import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.ts';
import type { GenreDto } from '../../types.ts';

export default function GenreList() {
  const [genres, setGenres] = useState<GenreDto[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.genres.list().then(setGenres).catch((e: Error) => setError(e.message));
  }, []);

  return (
    <div>
      <h1>Genres</h1>
      <Link to="/genres/new">Add new genre</Link>
      {error && <p className="error-message">{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {genres.map((genre) => (
            <tr key={genre.id}>
              <td>{genre.name}</td>
              <td className="actions">
                <Link to={`/genres/${genre.id}/edit`}>Edit</Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
