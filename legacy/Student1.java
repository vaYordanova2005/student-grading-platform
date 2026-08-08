import java.util.ArrayList;
import java.util.List;

public class Student1 extends User1{
    @Override
    public UserType getUserType(){
        return UserType.STUDENT;
    }

    List<Grade> grades = new ArrayList<>();
}
