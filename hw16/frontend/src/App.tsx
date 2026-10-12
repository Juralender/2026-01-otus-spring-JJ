import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom';
import BookList from './pages/books/BookList.tsx';
import BookDetail from './pages/books/BookDetail.tsx';
import BookForm from './pages/books/BookForm.tsx';
import AuthorList from './pages/authors/AuthorList.tsx';
import AuthorForm from './pages/authors/AuthorForm.tsx';
import GenreList from './pages/genres/GenreList.tsx';
import GenreForm from './pages/genres/GenreForm.tsx';

function Nav() {
  return (
    <nav>
      <Link to="/books">Books</Link>
      <Link to="/authors">Authors</Link>
      <Link to="/genres">Genres</Link>
    </nav>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Nav />
      <Routes>
        <Route path="/" element={<Navigate to="/books" replace />} />
        <Route path="/books" element={<BookList />} />
        <Route path="/books/new" element={<BookForm />} />
        <Route path="/books/:id" element={<BookDetail />} />
        <Route path="/books/:id/edit" element={<BookForm />} />
        <Route path="/authors" element={<AuthorList />} />
        <Route path="/authors/new" element={<AuthorForm />} />
        <Route path="/authors/:id/edit" element={<AuthorForm />} />
        <Route path="/genres" element={<GenreList />} />
        <Route path="/genres/new" element={<GenreForm />} />
        <Route path="/genres/:id/edit" element={<GenreForm />} />
      </Routes>
    </BrowserRouter>
  );
}
