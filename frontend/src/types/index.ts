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
  createdAt: string;
  teacherUsername: string | null;
}

export type CalendarEventType = 'TEST' | 'HOLIDAY' | 'EVENT';

export interface CalendarEventSummary {
  id: number;
  type: CalendarEventType;
  title: string;
  description: string | null;
  subject: string | null;
  startDate: string;
  endDate: string | null;
  createdByUsername: string;
  createdByRole: Role;
  createdAt: string;
}

export interface StudentProfileSummary {
  studentUsername: string;
  degreeLevel: string | null;
  facultyNumber: string | null;
  faculty: string | null;
  specialty: string | null;
  studyMode: string | null;
  specialization: string | null;
  groupNumber: string | null;
  admissionType: string | null;
  status: string | null;
  enrolledSemester: number | null;
  completedSemester: number | null;
  stream: string | null;
  personalEmail: string | null;
}
