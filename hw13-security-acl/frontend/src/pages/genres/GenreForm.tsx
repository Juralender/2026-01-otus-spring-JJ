import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, ApiError } from '../../api/client.ts';

export default function GenreForm() {
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [name, setName] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (!isEdit) return;
    api.genres.get(id!).then((genre) => setName(genre.name));
  }, [id, isEdit]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});
    const payload = { name };
    const action = isEdit ? api.genres.update(id!, payload) : api.genres.create(payload);
    action
      .then(() => navigate('/genres'))
      .catch((err: ApiError) => setErrors(err.errors || { general: err.message }));
  };

  return (
    <div>
      <h1>{isEdit ? 'Edit genre' : 'New genre'}</h1>
      <form onSubmit={handleSubmit}>
        {errors.general && <div className="error-message">{errors.general}</div>}

        <label htmlFor="name">Name</label>
        <input id="name" type="text" value={name} onChange={(e) => setName(e.target.value)} />
        {errors.name && <div className="error-message">{errors.name}</div>}

        <div className="buttons">
          <button type="submit">Save</button>
          <Link to="/genres">Cancel</Link>
        </div>
      </form>
    </div>
  );
}
