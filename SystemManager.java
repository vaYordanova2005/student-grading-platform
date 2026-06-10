import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemManager {
    List<User1> users = new ArrayList<>();
    Scanner sc = new Scanner(System.in);

    public void addUser(PrintStream ps){
        ps.println("What profile do you want to create: student / teacher? ");
        String profile = sc.nextLine();

        if(profile.equalsIgnoreCase("teacher")){
            ps.println("Enter email and password: ");
            String email = sc.nextLine();
            String password = sc.nextLine();

            Pattern pattern = Pattern.compile("^[a-z]+@tu-sofia\\.bg$");
            Matcher matcher = pattern.matcher(email);
            boolean match = matcher.matches();

            checkTeacher(match, password, email, ps);
        }else if(profile.equalsIgnoreCase("student")){
            ps.println("Enter username and EGN: ");
            String username = sc.nextLine();
            String egn = sc.nextLine();

            Pattern userPattern = Pattern.compile("^\\d{9}$");
            Matcher userMatcher = userPattern.matcher(username);
            boolean usernameValid = userMatcher.matches();

            Pattern egnPattern = Pattern.compile("^\\d{10}$");
            Matcher egnMatcher = egnPattern.matcher(egn);
            boolean egnValid = egnMatcher.matches();

            checkStudentRegex(usernameValid, egnValid, username, egn, ps);
        }else{
            ps.println("Invalid profile type.");
        }
    }

    public void checkStudentRegex(boolean usernameValid, boolean egnValid, String username, String egn, PrintStream ps){
        if(usernameValid && egnValid){
            Student1 student = new Student1();
            student.username = username;
            student.password = egn;
            createUser(student);
        }else{
            ps.println("Invalid username or EGN");
        }
    }

    public void checkTeacher(boolean match, String password, String email, PrintStream ps){
        if(match){
            if(password.length() >= 5){
                Teacher1 teacher = new Teacher1();
                teacher.username = email;
                teacher.password = password;
                createUser(teacher);
            }
        }else{
            ps.println("Invalid email");
        }
    }

    public void createUser(User1 user){
        synchronized(users) {
            users.add(user);
            saveUsers();
        }
    }

    public User1 login(String username, String password){
        for(User1 user : users){
            if(user.username.equalsIgnoreCase(username) && user.password.equalsIgnoreCase(password)){
                return user;
            }
        }
        return null;
    }

    public void addGrade(Teacher1 teacher, PrintStream ps){
        ps.println("Enter student FN, subject, semester and grade: ");
        String fn = sc.nextLine();
        String subject = sc.nextLine();
        int semester = Integer.parseInt(sc.nextLine());
        int gr = Integer.parseInt(sc.nextLine());
        boolean found = false;

        for(User1 student : users){
            if(student.username.equalsIgnoreCase(fn)){
                Grade grade = new Grade(subject, semester, gr);
                ((Student1) student).grades.add(grade);
                found = true;
                break;
            }
        }

        if(!found){
            ps.println("Invalid faculty number.");
        }
       synchronized(users) {
        saveUsers();
        }
    }

    public void showGrades(Student1 student, PrintStream ps){
        student.grades.sort(new SortGrades());
        for(Grade gr : student.grades){
            ps.println(gr);
        }
    }

    public void loadUsers(){
        try( FileInputStream fis = new FileInputStream("users.bin")) {
            ObjectInputStream ois = new ObjectInputStream(fis);
            users = (List<User1>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
        e.printStackTrace();
        }   
    }

    public void saveUsers(){  
       try(FileOutputStream fos = new FileOutputStream("users.bin")) {
           ObjectOutputStream oos = new ObjectOutputStream(fos);
           oos.writeObject(users);
       } catch (IOException e) {
        e.printStackTrace();
       }
    }

}

class SortGrades implements Comparator<Grade>{
    public int compare(Grade g1, Grade g2){
        if(g1.semester < g2.semester) return -1;
        else if(g1.semester > g2.semester) return 1;
        else return g1.subject.compareTo(g2.subject);
    }
}
