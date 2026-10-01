export interface AuthorDto {
  id: number;
  fullName: string;
}

export interface GenreDto {
  id: number;
  name: string;
}

export interface BookDto {
  id: number;
  title: string;
  author: AuthorDto;
  genres: GenreDto[];
}

export interface BookCommentDto {
  id: number;
  text: string;
  bookId: number;
}

export interface BookRequest {
  title: string;
  authorId: number;
  genreIds: number[];
}

export interface AuthorRequest {
  fullName: string;
}

export interface GenreRequest {
  name: string;
}

export interface BookCommentRequest {
  text: string;
}
