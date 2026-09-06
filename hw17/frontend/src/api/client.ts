import type {
  AuthorDto,
  AuthorRequest,
  BookCommentDto,
  BookCommentRequest,
  BookDto,
  BookRequest,
  GenreDto,
  GenreRequest,
} from '../types.ts';

interface ProblemDetailBody {
  detail?: string;
  errors?: Record<string, string>;
}

export class ApiError extends Error {
  status: number;

  errors?: Record<string, string>;

  constructor(message: string, status: number, errors?: Record<string, string>) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errors = errors;
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`/api${path}`, {
    ...options,
    headers: options.body ? { 'Content-Type': 'application/json' } : undefined,
  });

  if (!response.ok) {
    const problem: ProblemDetailBody | null = await response.json().catch(() => null);
    throw new ApiError(
      problem?.detail || `Request failed with status ${response.status}`,
      response.status,
      problem?.errors,
    );
  }

  if (response.status === 204) {
    return null as T;
  }
  return response.json() as Promise<T>;
}

export const api = {
  books: {
    list: () => request<BookDto[]>('/books'),
    get: (id: number | string) => request<BookDto>(`/books/${id}`),
    create: (data: BookRequest) => request<BookDto>('/books', { method: 'POST', body: JSON.stringify(data) }),
    update: (id: number | string, data: BookRequest) =>
      request<BookDto>(`/books/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    remove: (id: number | string) => request<null>(`/books/${id}`, { method: 'DELETE' }),
  },
  authors: {
    list: () => request<AuthorDto[]>('/authors'),
    get: (id: number | string) => request<AuthorDto>(`/authors/${id}`),
    create: (data: AuthorRequest) => request<AuthorDto>('/authors', { method: 'POST', body: JSON.stringify(data) }),
    update: (id: number | string, data: AuthorRequest) =>
      request<AuthorDto>(`/authors/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  },
  genres: {
    list: () => request<GenreDto[]>('/genres'),
    get: (id: number | string) => request<GenreDto>(`/genres/${id}`),
    create: (data: GenreRequest) => request<GenreDto>('/genres', { method: 'POST', body: JSON.stringify(data) }),
    update: (id: number | string, data: GenreRequest) =>
      request<GenreDto>(`/genres/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  },
  comments: {
    list: (bookId: number | string) => request<BookCommentDto[]>(`/books/${bookId}/comments`),
    create: (bookId: number | string, data: BookCommentRequest) =>
      request<BookCommentDto>(`/books/${bookId}/comments`, { method: 'POST', body: JSON.stringify(data) }),
    remove: (bookId: number | string, commentId: number | string) =>
      request<null>(`/books/${bookId}/comments/${commentId}`, { method: 'DELETE' }),
  },
};
