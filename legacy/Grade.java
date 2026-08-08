public class Grade {
    String subject;
    int semester;
    int grade;

    public Grade(String subject, int semester, int grade){
        this.subject = subject;
        this.semester = semester;
        this.grade = grade;
    }

    @Override
    public String toString()
    {
        return "Grade{" +
                "subject='" + subject + '\'' +
                ", semester=" + semester +
                ", grade=" + grade +
                '}';
    }
}
