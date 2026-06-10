public class Admin1 extends User1{
    @Override
    public UserType getUserType(){
        return UserType.ADMIN;
    }
}
