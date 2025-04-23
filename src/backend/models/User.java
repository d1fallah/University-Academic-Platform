
package backend.models;

/**
 *
 * @author akram
 */
import java.sql.Timestamp;
public class User {


        private int id;
        private String name;
        private String password;
        private String matricule;
        private String role;
        private Timestamp createdAt;


    // Constructors

        public User(){}

    public User(int id, String name, String password, String matricule, String role, Timestamp createdAt){
        this.id = id;
        this.name = name;
        this.password = password;
        this.matricule = matricule;
        this.role = role;
        this.createdAt = createdAt;
    }

    public User(String name, String password, String matricule, String role){
        this.name= name;
        this.password = password;
        this.matricule = matricule;
        this.role = role;
    }

    // Getter and Setters

    public int getId(){
        return id;
    }

    public void setId(int id){
    this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role){
        // Validating Role
        if(role.equals("student") || role.equals("teacher")){
            this.role = role;
        }else{
            throw new IllegalArgumentException("Role must be 'student' or 'teacher'");
        }
    }

    public Timestamp getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt){
        this.createdAt = createdAt;
    }
}

