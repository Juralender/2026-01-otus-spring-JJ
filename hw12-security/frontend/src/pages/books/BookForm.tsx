import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, ApiError } from '../../api/client.ts';
import type { AuthorDto, GenreDto } from '../../types.ts';

export default function BookForm() {
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [title, setTitle] = useState('');
  const [authorId, setAuthorId] = useState('');
  const [genreIds, setGenreIds] = useState<string[]>([]);
  const [authors, setAuthors] = useState<AuthorDto[]>([]);
  const [genres, setGenres] = useState<GenreDto[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    api.authors.list().then(setAuthors);
    api.genres.list().then(setGenres);
  }, []);

  useEffect(() => {
    if (!isEdit) return;
    api.books.get(id!).then((book) => {
      setTitle(book.title);
      setAuthorId(String(book.author.id));
      setGenreIds(book.genres.map((g) => String(g.id)));
    });
  }, [id, isEdit]);

  const handleGenresChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setGenreIds(Array.from(e.target.selectedOptions, (o) => o.value));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});
    const payload = { title, authorId: Number(authorId), genreIds: genreIds.map(Number) };
    const action = isEdit ? api.books.update(id!, payload) : api.books.create(payload);
    action
      .then(() => navigate('/books'))
      .catch((err: ApiError) => setErrors(err.errors || { general: err.message }));
  };

  return (
    <div>
      <h1>{isEdit ? 'Edit book' : 'New book'}</h1>
      <form onSubmit={handleSubmit}>
        {errors.general && <div className="error-message">{errors.general}</div>}

        <label htmlFor="title">Title</label>
        <input id="title" type="text" value={title} onChange={(e) => setTitle(e.target.value)} />
        {errors.title && <div className="error-message">{errors.title}</div>}

        <label htmlFor="authorId">Author</label>
        <select id="authorId" value={authorId} onChange={(e) => setAuthorId(e.target.value)}>
          <option value="" disabled>Select author</option>
          {authors.map((a) => (
            <option key={a.id} value={a.id}>{a.fullName}</option>
          ))}
        </select>
        {errors.authorId && <div className="error-message">{errors.authorId}</div>}

        <label htmlFor="genreIds">Genres</label>
        <select id="genreIds" multiple value={genreIds} onChange={handleGenresChange}>
          {genres.map((g) => (
            <option key={g.id} value={g.id}>{g.name}</option>
          ))}
        </select>
        {errors.genreIds && <div className="error-message">{errors.genreIds}</div>}

        <div className="buttons">
          <button type="submit">Save</button>
          <Link to="/books">Cancel</Link>
        </div>
      </form>
    </div>
  );
}
