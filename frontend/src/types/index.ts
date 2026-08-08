export type Role = 'ADMIN' | 'TEACHER' | 'STUDENT';

export interface AuthUser {
  username: string;
  role: Role;
  token: string;
}

export interface UserSummary {
  id: number;
  username: string;
  role: Role;
}

export interface GradeSummary {
  id: number;
  subject: string;
  semester: number;
  grade: number;
}
