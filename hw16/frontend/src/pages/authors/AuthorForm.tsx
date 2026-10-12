import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, ApiError } from '../../api/client.ts';

export default function AuthorForm() {
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [fullName, setFullName] = useState('');
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (!isEdit) return;
    api.authors.get(id!).then((author) => setFullName(author.fullName));
  }, [id, isEdit]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});
    const payload = { fullName };
    const action = isEdit ? api.authors.update(id!, payload) : api.authors.create(payload);
    action
      .then(() => navigate('/authors'))
      .catch((err: ApiError) => setErrors(err.errors || { general: err.message }));
  };

  return (
    <div>
      <h1>{isEdit ? 'Edit author' : 'New author'}</h1>
      <form onSubmit={handleSubmit}>
        {errors.general && <div className="error-message">{errors.general}</div>}

        <label htmlFor="fullName">Full name</label>
        <input id="fullName" type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} />
        {errors.fullName && <div className="error-message">{errors.fullName}</div>}

        <div className="buttons">
          <button type="submit">Save</button>
          <Link to="/authors">Cancel</Link>
        </div>
      </form>
    </div>
  );
}
